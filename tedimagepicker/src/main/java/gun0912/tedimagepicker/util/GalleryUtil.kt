package gun0912.tedimagepicker.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.builder.type.MediaType
import gun0912.tedimagepicker.model.Album
import gun0912.tedimagepicker.model.Media
import io.reactivex.Single
import java.io.File

internal class GalleryUtil {
    companion object {

        private const val INDEX_MEDIA_ID = MediaStore.MediaColumns._ID
        private const val INDEX_MEDIA_URI = MediaStore.MediaColumns.DATA
        private const val INDEX_DATE_ADDED = MediaStore.MediaColumns.DATE_ADDED
        private const val INDEX_ALBUM_NAME = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        private const val INDEX_DURATION = MediaStore.MediaColumns.DURATION

        internal fun getMedia(context: Context, mediaType: MediaType): Single<List<Album>> {
            return Single.create { emitter ->
                try {

                    val totalMediaList: List<Media> = when (mediaType) {
                        MediaType.IMAGE -> getAllMediaList(context, QueryMediaType.IMAGE)
                        MediaType.VIDEO -> getAllMediaList(context, QueryMediaType.VIDEO)
                        MediaType.IMAGE_AND_VIDEO -> {
                            val imageMediaList = getAllMediaList(context, QueryMediaType.IMAGE)
                            val videoMediaList = getAllMediaList(context, QueryMediaType.VIDEO)
                            (imageMediaList + videoMediaList).sortedByDescending { it.dateAddedSecond }
                        }
                    }
                    val albumList: List<Album> = totalMediaList
                        .groupBy { media: Media -> media.albumName }
                        .toSortedMap { albumName1: String, albumName2: String ->
                            if (albumName2 == "Camera") {
                                1
                            } else {
                                albumName1.compareTo(albumName2, true)
                            }
                        }
                        .map { entry -> getAlbum(entry) }
                        .toList()


                    val totalAlbum = totalMediaList.run {
                        val albumName = context.getString(R.string.ted_image_picker_album_all)
                        Album(
                            albumName,
                            firstOrNull()?.uri ?: Uri.EMPTY,
                            this
                        )
                    }

                    val result = mutableListOf(totalAlbum).apply { addAll(albumList) }
                    emitter.onSuccess(result)
                } catch (exception: Exception) {
                    emitter.onError(exception)
                }
            }
        }

        private fun getAllMediaList(context: Context, queryMediaType: QueryMediaType): List<Media> {
            val sortOrder = "$INDEX_DATE_ADDED DESC"

            val projection = mutableListOf(
                INDEX_MEDIA_ID,
                INDEX_MEDIA_URI,
                INDEX_ALBUM_NAME,
                INDEX_DATE_ADDED,
            ).apply {
                if (queryMediaType == QueryMediaType.VIDEO) {
                    add(INDEX_DURATION)
                }
            }.toTypedArray()
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.SIZE + " > 0"
            } else {
                null
            }
            val cursor =
                context.contentResolver.query(
                    queryMediaType.contentUri,
                    projection,
                    selection,
                    null,
                    sortOrder
                )
                    ?: return emptyList()

            cursor.use {
                return generateSequence { if (cursor.moveToNext()) cursor else null }
                    .map { getMedia(it, queryMediaType) }
                    .filterNotNull()
                    .toList()
            }
        }

        private fun getAlbum(entry: Map.Entry<String, List<Media>>) =
            Album(entry.key, entry.value[0].uri, entry.value)

        private fun getMedia(cursor: Cursor, queryMediaType: QueryMediaType): Media? =
            try {
                cursor.run {
                    val albumName = getString(getColumnIndexOrThrow(INDEX_ALBUM_NAME))
                    val mediaUri = getMediaUri(queryMediaType)
                    val datedAddedSecond = getLong(getColumnIndexOrThrow(INDEX_DATE_ADDED))
                    when (queryMediaType) {
                        QueryMediaType.IMAGE ->
                            Media.Image(albumName, mediaUri, datedAddedSecond)
                        QueryMediaType.VIDEO -> {
                            val duration = getLong(getColumnIndexOrThrow(INDEX_DURATION))
                            Media.Video(albumName, mediaUri, datedAddedSecond, duration)
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                null
            }

        private fun Cursor.getMediaUri(queryMediaType: QueryMediaType): Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val id = getLong(getColumnIndexOrThrow(INDEX_MEDIA_ID))
                ContentUris.withAppendedId(queryMediaType.contentUri, id)
            } else {
                val mediaPath = getString(getColumnIndexOrThrow(INDEX_MEDIA_URI))
                Uri.fromFile(File(mediaPath))
            }

        private enum class QueryMediaType(val contentUri: Uri) {
            IMAGE(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            VIDEO(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        }
    }
}

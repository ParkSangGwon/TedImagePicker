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
import java.util.Collections.addAll

internal class GalleryUtil {
    companion object {

        private const val INDEX_MEDIA_ID = MediaStore.MediaColumns._ID
        private const val INDEX_MEDIA_URI = MediaStore.MediaColumns.DATA
        private const val INDEX_DATE_ADDED = MediaStore.MediaColumns.DATE_ADDED

        private lateinit var albumName: String

        internal fun getMedia(context: Context, mediaType: MediaType): Single<List<Album>> {
            return Single.create { emitter ->
                try {
                    val totalMediaList = getAllMediaList(context, mediaType)
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
                            getOrElse(0) { Media(albumName, Uri.EMPTY, 0) }.uri,
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

        private fun getAllMediaList(context: Context, mediaType: MediaType): List<Media> {
            val uri: Uri
            when (mediaType) {
                MediaType.IMAGE -> {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    albumName = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                }
                MediaType.VIDEO -> {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    albumName = MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                }
            }

            val sortOrder = "$INDEX_DATE_ADDED DESC"

            val projection = arrayOf(
                INDEX_MEDIA_ID,
                INDEX_MEDIA_URI,
                albumName,
                INDEX_DATE_ADDED
            )
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.SIZE + " > 0"
            } else {
                null
            }
            val cursor =
                context.contentResolver.query(uri, projection, selection, null, sortOrder)
                    ?: return emptyList()

            cursor.use {
                return generateSequence { if (cursor.moveToNext()) cursor else null }
                    .map { getMedia(it, mediaType) }
                    .filterNotNull()
                    .toList()
            }
        }

        private fun getAlbum(entry: Map.Entry<String, List<Media>>) =
            Album(entry.key, entry.value[0].uri, entry.value)

        private fun getMedia(cursor: Cursor, mediaType: MediaType): Media? =
            try {
                cursor.run {
                    val folderName = getString(getColumnIndexOrThrow(albumName))
                    val mediaUri = getMediaUri(mediaType)
                    val datedAddedSecond = getLong(getColumnIndexOrThrow(INDEX_DATE_ADDED))
                    Media(folderName, mediaUri, datedAddedSecond)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                null
            }

        private fun Cursor.getMediaUri(mediaType: MediaType): Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val id = getLong(getColumnIndexOrThrow(INDEX_MEDIA_ID))

                val contentUri = when (mediaType) {
                    MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                ContentUris.withAppendedId(contentUri, id)
            } else {
                val mediaPath = getString(getColumnIndexOrThrow(INDEX_MEDIA_URI))
                Uri.fromFile(File(mediaPath))
            }
    }
}

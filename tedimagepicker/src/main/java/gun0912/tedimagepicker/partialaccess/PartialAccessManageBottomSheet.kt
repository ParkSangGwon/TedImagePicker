package gun0912.tedimagepicker.partialaccess

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gun0912.tedpermission.TedPermissionUtil
import com.gun0912.tedpermission.rx2.TedPermission
import com.tedpark.tedonactivityresult.rx2.TedRxOnActivityResult
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.builder.type.MediaType
import gun0912.tedimagepicker.databinding.BottomsheetPartialAccessManageBinding
import gun0912.tedimagepicker.util.isFullOrPartialAccessGranted

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class PartialAccessManageBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetPartialAccessManageBinding

    private lateinit var mediaType: MediaType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaType = requireArguments().getParcelable(ARGUMENT_MEDIA_TYPE) ?: MediaType.IMAGE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = BottomsheetPartialAccessManageBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupText()
        setupListener()
    }

    private fun setupText() = with(binding) {
        val mediaText = getString(mediaType.nameResId)

        tvSelectMorePhotoVideo.text =
            getString(R.string.ted_image_picker_select_more_photo_video_fmt, mediaText)
        tvGrantFullAccessPhotoVideo.text =
            getString(R.string.ted_image_picker_grant_full_access_photo_video_fmt, mediaText)
    }

    private fun setupListener() {
        binding.tvCancel.setOnClickListener { dismiss() }
        binding.tvSelectMorePhotoVideo.setOnClickListener { selectMoreImageVideo() }
        binding.tvGrantFullAccessPhotoVideo.setOnClickListener { grantFullAccess() }
    }

    private fun selectMoreImageVideo() {
        requestPermission()
    }

    @SuppressLint("CheckResult")
    private fun grantFullAccess() {
        val canRequestPermission =
            TedPermissionUtil.canRequestPermission(requireActivity(), *mediaType.permissions)
        if (canRequestPermission) {
            requestPermission()
        } else {
            TedRxOnActivityResult.with(activity)
                .startActivityForResult(TedPermissionUtil.getSettingIntent())
                .subscribe { _ ->
                    if (TedPermissionUtil.isGranted(*mediaType.permissions)) {
                        actionComplete()
                    } else {
                        dismiss()
                    }
                }
        }
    }

    @SuppressLint("CheckResult")
    private fun requestPermission() {
        TedPermission.create()
            .setPermissions(*mediaType.permissions)
            .request()
            .subscribe { _ ->
                if (mediaType.isFullOrPartialAccessGranted) {
                    actionComplete()
                }
            }
    }

    private fun actionComplete() {
        (activity as? Listener)?.onRefreshMedia()
        dismiss()
    }

    interface Listener {
        fun onRefreshMedia()
    }

    companion object {
        private const val ARGUMENT_MEDIA_TYPE = "ARGUMENT_MEDIA_TYPE"
        private val TAG = this::class.java.simpleName

        fun show(activity: FragmentActivity, mediaType: MediaType) {
            PartialAccessManageBottomSheet().apply {
                arguments = bundleOf(ARGUMENT_MEDIA_TYPE to mediaType)
            }.show(activity.supportFragmentManager, TAG)
        }
    }
}

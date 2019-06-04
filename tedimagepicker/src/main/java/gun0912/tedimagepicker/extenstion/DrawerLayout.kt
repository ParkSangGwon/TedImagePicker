package gun0912.tedimagepicker.extenstion

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

fun DrawerLayout.close() {
    if (isOpen()) {
        closeDrawer(GravityCompat.START)
    }
}


fun DrawerLayout.open() {
    if (!isOpen()) {
        openDrawer(GravityCompat.START)
    }
}

fun DrawerLayout.isOpen() = isDrawerOpen(GravityCompat.START)

fun DrawerLayout.toggle() {
    if (isOpen()) {
        close()
    } else {
        open()
    }
}

fun DrawerLayout.setLock(lock: Boolean) {
    if (lock) {
        setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
    } else {
        setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }
}
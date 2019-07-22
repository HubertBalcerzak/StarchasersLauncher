package ovh.snet.starchaserslauncher.gameLauncher

import java.io.InputStream
import java.io.PrintStream
import java.util.*

object IOManager {
    fun inheritIO(src: InputStream, dest: PrintStream) {
        Thread(Runnable {
            val sc = Scanner(src)
            while (sc.hasNextLine()) {
                dest.println(sc.nextLine())
            }
            sc.close()
        }).apply {
            isDaemon = true
        }.start()
    }
}
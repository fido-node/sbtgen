package me.fidonode

import com.whisk.util.logging.zio.ZioLogging.Logging
import com.whisk.util.zio.app.WhiskApp
import com.whisk.util.zio.config.{ConfigLoader, Descriptor}
import zio.{Scope, ZIO}

class FridgeApp extends AwesomeApp[Unit] {
  override protected def run(
                              configLoader: ConfigLoader[Unit]
                            ): ZIO[Logging with Scope, Throwable, Any] = {
    ZIO
      .succeed(
        """
          |***
          |I'm a little teapot,
          |Short and stout,
          |Here is my handle
          |Here is my spout
          |When I get all steamed up,
          |Hear me shout,
          |Tip me over and pour me out!
          |***
          |""".stripMargin)
      .tap(teapotSong =>
        ZIO.log(teapotSong)
      )
  }
}

object FridgeApp extends FridgeApp {}
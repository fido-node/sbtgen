package me.fidonode

import com.whisk.util.locale.Country
import com.whisk.util.logging.zio.ZioLogging.Logging
import com.whisk.util.zio.config.ConfigLoader
import zio.{Scope, ZIO}

class CooktopApp extends AwesomeApp[Unit] {
  override protected def run(
      configLoader: ConfigLoader[Unit]
  ): ZIO[Logging with Scope, Throwable, Any] = {
    ZIO
      .succeed(Country.values)
      .tap(countryList =>
        ZIO.log(s"I know only next countries: ${countryList.mkString(", ")}")
      )
  }
}

object CooktopApp extends CooktopApp {}
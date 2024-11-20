package me.fidonode

import com.whisk.util.zio.app.WhiskApp
import com.whisk.util.zio.config.Descriptor

abstract class AwesomeApp[C: Descriptor] extends WhiskApp[C] {}

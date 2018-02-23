package io.github.rcarlosdasilva.wenger.ms.upms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@ComponentScan(value = ["io.github.rcarlosdasilva.wenger"])
@EnableCaching
@EnableAsync
open class Bootstrap {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      runApplication<Bootstrap>(*args)
    }
  }

}

package io.github.rcarlosdasilva.wenger.config


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.config.server.EnableConfigServer

@SpringBootApplication
@EnableConfigServer
open class Bootstrap {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      runApplication<Bootstrap>(*args)
    }
  }

}
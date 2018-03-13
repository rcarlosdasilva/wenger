package io.github.rcarlosdasilva.wenger.registry


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@SpringBootApplication
@EnableEurekaServer
open class Bootstrap {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      runApplication<Bootstrap>(*args)
    }
  }

}
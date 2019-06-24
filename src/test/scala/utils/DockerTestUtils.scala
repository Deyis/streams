package utils

import org.scalatest.ConfigMap
import org.scalatest.exceptions.TestFailedException

object DockerTestUtils {

  private val serviceName = "app"
  private val serviceHostKey = s"$serviceName:9000"
  private val serviceContainerIdKey = s"$serviceName:containerId"

  def getAppHost(configMap: ConfigMap): String =
    getContainerSetting(configMap, serviceHostKey)

  def getAppContainerId(configMap: ConfigMap): String =
    getContainerSetting(configMap, serviceContainerIdKey)

  def getContainerSetting(configMap: ConfigMap, key: String): String =
    if (configMap.keySet.contains(key))
      configMap(key).toString
    else
      throw new TestFailedException(s"Cannot find the expected Docker Compose service key '$key' in the configMap", 10)
}

object Versions {
  val circe          = "0.14.9"
  val neutron        = "0.8.0"
  val pulsarClient   = "3.1.0"
  val zio            = "2.1.6"
  val zioStreams     = "2.1.1"
  val zioLogging     = "2.3.0"
  val zioCatsInterop = "23.1.0.2"
  val zioHttp        = "2.0.0-RC11" // TODO: bump to "3.0.0-RC9" (also change artefact)
  // Note: slf4j is pinned to '1.7.36' because it's used by pulsar-client:3.1.0 (transitive dependency of neutron:0.8.0)
  val slf4j      = "1.7.36"
  val cats       = "2.12.0"
  val catsEffect = "3.5.4"
  val munit      = "1.0.0"
  val fs2        = "3.10.2"

}

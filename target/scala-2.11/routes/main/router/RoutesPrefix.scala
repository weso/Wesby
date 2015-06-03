
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/jorge/Desarrollador/Wesby/conf/routes
// @DATE:Wed Jun 03 18:45:58 CEST 2015


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}

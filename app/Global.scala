import play.api.GlobalSettings
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter) with GlobalSettings {

}
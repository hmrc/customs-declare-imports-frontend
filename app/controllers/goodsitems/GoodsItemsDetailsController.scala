package controllers.goodsitems

import config.AppConfig
import controllers.{Actions, CustomsController}
import domain.DeclarationFormats._
import forms.DeclarationFormMapping._
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import services.cachekeys.CacheKey
import uk.gov.hmrc.wco.dec.Commodity
import views.html.goodsitems.goods_items_commodity_details

import scala.concurrent.{ExecutionContext, Future}

class GoodsItemsDetailsController @Inject()(actions: Actions, cacheService: CustomsCacheService)
                                           (implicit appConfig: AppConfig, override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends CustomsController  {

}

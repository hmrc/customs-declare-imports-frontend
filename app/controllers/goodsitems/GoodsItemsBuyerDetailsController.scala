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
import uk.gov.hmrc.wco.dec.ImportExportParty
import views.html.goodsitems.goods_items_buyer_details
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
import uk.gov.hmrc.wco.dec.ImportExportParty
import scala.concurrent.{ExecutionContext, Future}

class GoodsItemsBuyerDetailsController @Inject()(actions: Actions, cacheService: CustomsCacheService)
                                                (implicit appConfig: AppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 ec: ExecutionContext) extends CustomsController {
  val form = Form(importExportPartyMapping)

  def onPageLoad: Action[AnyContent] = (actions.auth andThen actions.eori andThen actions.goodsItem) { implicit req =>

    val popForm = req.goodsItem.seller.fold(form)(form.fill)
    Ok(goods_items_buyer_details(popForm))
  }

  def onSubmit: Action[AnyContent] = (actions.auth andThen actions.eori andThen actions.goodsItem).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[ImportExportParty]) =>
          Future.successful(BadRequest(goods_items_buyer_details(formWithErrors))),
        sellerDetails => {
          val updatedGoodsItem = request.goodsItem.copy(seller = Some(sellerDetails))

          cacheService.insert(request.eori, CacheKey.goodsItem, updatedGoodsItem).map { _ =>
            Redirect(controllers.routes.GovernmentAgencyGoodsItemsController.showGoodsItemPage())
          }
        })
  }
}

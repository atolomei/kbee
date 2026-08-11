package com.novamens.kbee.content.webapi.payment;

import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import kbee.payment.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/mp/*")
public class MercadoPagoCallbacks {

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeMercadoPagoPaymentService.class.getName());

    @ApiOperation(
            value = "Process notifications",
            authorizations = {@Authorization(value = "basicAuth")})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok")})
    @RequestMapping(value = "notify", method = RequestMethod.POST)

    public void notify(@RequestParam(value = "id") String id,
                       @RequestParam(value = "topic") String topic,
                       @RequestBody MercadoPagoNotification mercadoPagoNotification) {

        logger.info("Processing MercadoPago notification id:" + id + ", topic:" + topic + ". " + mercadoPagoNotification.toString());
        if ("payment".equals(topic)) {
            Transaction trx = null;
            try {

                trx = beginTransaction();
                ServiceLocator.getService(SchedulerService.class).enqueue(new ProcessMercadoPagoPaymentRequest(id));
                trx.commit();
            } catch (SchedulerException e) {
                trx.rollback();
                logger.error(e);
            }
        } else {
            logger.info("MercadoPago notification ignored.");
        }
    }

    @GetMapping("/paymentSuccess")
    public void paymentSuccess(HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestParam("collection_id") String collectionId,
                               @RequestParam("collection_status") String collectionStatus,
                               @RequestParam("external_reference") String externalReference,
                               @RequestParam("payment_type") String paymentType,
                               @RequestParam("merchant_order_id") String merchantOrderId,
                               @RequestParam("preference_id") String preferenceId,
                               @RequestParam("site_id") String siteId,
                               @RequestParam("processing_mode") String processingMode,
                               @RequestParam("merchant_account_id") String merchantAccountId,
                               @RequestParam("payment_id") String paymentId) {
        handlePaymentBackRequest(request, response, collectionId, collectionStatus, externalReference, paymentType, merchantOrderId, preferenceId, siteId, processingMode, merchantAccountId, paymentId, BackRequestType.Success);
    }

    @GetMapping("/paymentFailure")
    public void paymentFailure(HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestParam("collection_id") String collectionId,
                               @RequestParam("collection_status") String collectionStatus,
                               @RequestParam("external_reference") String externalReference,
                               @RequestParam("payment_type") String paymentType,
                               @RequestParam("merchant_order_id") String merchantOrderId,
                               @RequestParam("preference_id") String preferenceId,
                               @RequestParam("site_id") String siteId,
                               @RequestParam("processing_mode") String processingMode,
                               @RequestParam("merchant_account_id") String merchantAccountId,
                               @RequestParam("payment_id") String paymentId) {
        handlePaymentBackRequest(request, response, collectionId, collectionStatus, externalReference, paymentType, merchantOrderId, preferenceId, siteId, processingMode, merchantAccountId, paymentId, BackRequestType.Failure);
    }

    @GetMapping("/paymentPending")
    public void paymentPending(HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestParam("collection_id") String collectionId,
                               @RequestParam("collection_status") String collectionStatus,
                               @RequestParam("external_reference") String externalReference,
                               @RequestParam("payment_type") String paymentType,
                               @RequestParam("merchant_order_id") String merchantOrderId,
                               @RequestParam("preference_id") String preferenceId,
                               @RequestParam("site_id") String siteId,
                               @RequestParam("processing_mode") String processingMode,
                               @RequestParam("merchant_account_id") String merchantAccountId,
                               @RequestParam("payment_id") String paymentId) {
        handlePaymentBackRequest(request, response, collectionId, collectionStatus, externalReference, paymentType, merchantOrderId, preferenceId, siteId, processingMode, merchantAccountId, paymentId, BackRequestType.Pending);
    }


    private void handlePaymentBackRequest(HttpServletRequest request, HttpServletResponse response, String collectionId, String collectionStatus,
                                          String externalReference, String paymentType, String merchantOrderId, String preferenceId, String siteId,
                                          String processingMode, String merchantAccountId, String paymentId, BackRequestType backRequestType) {
        try {
            if (externalReference != null && paymentId != null) {
                final MercadoPagoPaymentService mercadoPagoPaymentService = ServiceLocator.getService(MercadoPagoPaymentService.class);
                final Payment kbPayment = mercadoPagoPaymentService.findPaymentByTrxReference(externalReference);

                if (backRequestType == BackRequestType.Success && kbPayment.getStatus() != PaymentStatus.CONFIRMED) {
                    final com.mercadopago.resources.Payment externalPayment = mercadoPagoPaymentService.findExternalPayment(paymentId);
                    mercadoPagoPaymentService.tryProcessPayment(externalPayment);
                }
                response.sendRedirect(kbPayment.getRedirectUrl());
            }
        } catch (Exception e) {
            logger.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected Transaction beginTransaction() {
        return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
    }

    private enum BackRequestType {
        Success, Pending, Failure
    }

}

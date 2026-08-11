package kbee.payment;

import com.mercadopago.resources.Payment;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class ProcessMercadoPagoPaymentRequest extends AbstractServiceRequest {


    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ProcessMercadoPagoPaymentRequest.class.getName());
    private String externalMercadoPagoPaymentId;

    public ProcessMercadoPagoPaymentRequest() {
            setPriority(SchedulerService.HIGH_PRIORITY);
            setCost(SchedulerService.STANDARD_PROCESSING_COST);
            setName(this.getClass().getSimpleName());
    }

    public ProcessMercadoPagoPaymentRequest(String externalMercadoPagoPaymentId) {
        super();
        this.externalMercadoPagoPaymentId = externalMercadoPagoPaymentId;
    }

    @Override
    public void execute() {
        final MercadoPagoPaymentService paymentService = ServiceLocator.getService(MercadoPagoPaymentService.class);

        final Payment externalPayment = paymentService.findExternalPayment(getExternalMercadoPagoPaymentId());
        try {
            paymentService.tryProcessPayment(externalPayment);
        }catch (Exception e){
            throw new KbeeRuntimeException(e);
        }
    }


    public String getExternalMercadoPagoPaymentId() {
        return externalMercadoPagoPaymentId;
    }

    public void setExternalMercadoPagoPaymentId(String externalMercadoPagoPaymentId) {
        this.externalMercadoPagoPaymentId = externalMercadoPagoPaymentId;
    }
}

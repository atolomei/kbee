package com.novamens.kbee.wicket.util;

import jp.try0.wicket.toastr.core.Toast;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class FeedbackHelper {
    public static void showSuccessToast(String title, String message){
        final AjaxRequestTarget ajaxRequest = getAjaxRequest();
        if(ajaxRequest != null) {
            Toast.create(Toast.ToastLevel.SUCCESS, parse(message))
                    .withTitle(title)
                    .show(ajaxRequest);
        }
    }

    public static void showSuccessToast(String message){
        final AjaxRequestTarget ajaxRequest = getAjaxRequest();
        if(ajaxRequest != null) {
        	String msg = parse(message);
            Toast.create(Toast.ToastLevel.SUCCESS, msg)
                    .show(ajaxRequest);
        }
    }

    public static void showInfoToast(String title, String message){
        final AjaxRequestTarget ajaxRequest = getAjaxRequest();
        if(ajaxRequest != null) {
        	String msg = parse(message);
            Toast.create(Toast.ToastLevel.INFO, msg)
                    .withTitle(title)
                    .show(ajaxRequest);
        }
    }

    public static void showInfoToast(String message){
        final AjaxRequestTarget ajaxRequest = getAjaxRequest();
        if(ajaxRequest != null) {
        	
        	String msg = parse(message);
        	//ToastOptions options = ToastOptions.create();
        	//options.setHideDuration(2000);
        	//options.setShowDuration(2000);
        	//options.setExtendedTimeOut(2000);
        	//options.setPositionClass(ToastOptions.PositionClass.BOTTOM_RIGHT);
        	//options.setIsEnableProgressBar(true);
        	//options.setIsEnableCloseButton(true);
        	Toast.create(Toast.ToastLevel.INFO, msg).show(ajaxRequest);
        }
    }

    public static void showErrorToast(String title, String message){
        final AjaxRequestTarget ajaxRequest = getAjaxRequest();
        if(ajaxRequest != null) {
        	String msg = parse(message);
            Toast.create(Toast.ToastLevel.ERROR, msg)
                    .withTitle(title)
                    .show(ajaxRequest);
        }
    }

    public static void showErrorToast(String message){
        final AjaxRequestTarget ajaxRequest = getAjaxRequest();
        if(ajaxRequest != null) {
        	String msg = parse(message);
            Toast.create(Toast.ToastLevel.ERROR, msg)
                    .show(ajaxRequest);
        }
    }


    private static AjaxRequestTarget getAjaxRequest(){
        final Optional<AjaxRequestTarget> ajaxRequestTarget = RequestCycle.get().find(AjaxRequestTarget.class);
        return ajaxRequestTarget.orElse(null);
    }
    
    /**
     * <p> remove the string {@code <span class="ago">} that breaks the toast</p>
     * @param s
     * @return
     */
	private static String parse(String s) {
		return  (s==null) ? null : Jsoup.clean(s, Safelist.basic());
	}

}

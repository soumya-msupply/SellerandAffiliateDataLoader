import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class loadThread implements Runnable {

	@Override
	public void run() {
		try {
			URL url = new URL("http://localhost:8084/supplier/api/v1.0/insertOrder");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			String input = "%7B%20%20%0A%20%20%20%22orderInfo%22%3A%7B%20%0A%20%22orderPlatform%22%3A%22mobile%2Fandroid%22%2C%20%0A%20%20%20%20%20%20%22orderDeliveryAddress%22%3A%5B%20%20%0A%20%20%20%20%20%20%20%20%20%7B%20%20%0A%20%20%20%20%20%20%20%20%20%20%20%20%22addressType%22%3A%22Billing%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22ContactfirstName%22%3A%22Reshma%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22ContactlastName%22%3A%22Hugar%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22Contactmobile%22%3A%2212345%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22ContactemailId%22%3A%22abcd.com%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22PAN%22%3A%22PAN888%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22TIN%22%3A%22TIN888%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22addressLine1%22%3A%22msupply%2C%20Sector%202%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22addressLine2%22%3A%22HSR%20Layout%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22city%22%3A%22Bangalore%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22state%22%3A%22Karnataka%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22country%22%3A%22India%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22pinCode%22%3A%22660001%22%0A%20%20%20%20%20%20%20%20%20%7D%2C%0A%20%20%20%20%20%20%20%20%20%7B%20%20%0A%20%20%20%20%20%20%20%20%20%20%20%20%22addressType%22%3A%22Shipping%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22ContactfirstName%22%3A%22ABC%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22ContactlastName%22%3A%22BAC%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22Contactmobile%22%3A%2212345%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22ContactemailId%22%3A%22abc.com%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22addressLine1%22%3A%22Agra%2C%20Sector%202%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22addressLine2%22%3A%22HSR%20Layout%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22city%22%3A%22Bangalore%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22state%22%3A%22Karnataka%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22country%22%3A%22India%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22pinCode%22%3A%22660001%22%0A%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%20%5D%2C%0A%20%20%20%20%20%20%22customerInfo%22%3A%7B%20%20%0A%20%20%20%20%20%20%20%20%20%22customerId%22%3A%22cust123%22%0A%20%20%20%20%20%20%7D%2C%0A%20%20%20%20%20%20%22orderItemInfo%22%3A%5B%20%20%0A%20%20%20%20%20%20%20%20%20%7B%20%20%0A%20%20%20%20%20%20%20%20%20%20%20%20%22sellerId%22%3A%22S10019%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22sku%22%3A%22SKU0111%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22skuName%22%3A%22Steel%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22skuImageURL%22%3A%22http%2F%2F123.com%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22offerPriceUnit%22%3A%22Bags%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22estimatedDeliveryDays%22%3A%222-3%22%2C%0A%20%20%20%20%20%20%20%20%20%20%20%20%22qtyUnit%22%3A%22Bags%22%0A%20%20%20%20%20%20%20%20%20%7D%0A%20%20%20%20%20%0A%20%20%20%20%20%20%5D%0A%0A%20%20%20%7D%2C%0A%20%20%20%22paymentInfo%22%3A%7B%20%20%0A%20%20%20%20%20%20%22amountPaid%22%3A10000.05%2C%0A%20%20%20%20%20%20%22currency%22%3A%22INR%22%2C%0A%20%20%20%20%20%20%22status%22%3A%22PAID%22%2C%0A%20%20%20%20%20%20%22paymentMode%22%3A%22PayUMoney%22%2C%20%0A%20%20%20%20%20%20%22transactionId%22%3A%22%22%20%0A%20%20%20%7D%2C%0A%22kartMessage%22%3A%20%7B%22pincode%22%3A%22560001%22%2C%22KartInfo%22%3A%5B%7B%22unitPrice%22%3A2016%2C%22exciseAmountOnUnitPrice%22%3A34.272000000000006%2C%22VATAmountOnUnitPrice%22%3A0%2C%22excise%22%3A1.7%2C%22VAT%22%3A0%2C%22skuWeightUnit%22%3A%22Kilograms%22%2C%22sellerId%22%3A%22S10019%22%2C%22VATBasis%22%3A%22Percentage%22%2C%22subtotal%22%3A94752%2C%22qty%22%3A47%2C%22sku%22%3A%22SKU0111%22%2C%22exciseBasis%22%3A%22Percentage%22%2C%22brickId%22%3A%22B100020%22%2C%22skuWeight%22%3A456%7D%5D%2C%22SellerChargesConsolidation%22%3A%5B%7B%22sellerId%22%3A%22S10019%22%2C%22Customer%22%3A%7B%22shippingAndHandlingCharges%22%3A0%2C%22shippingAndHandlingAnd3PLCharges%22%3A170%2C%22total%22%3A96532.784%2C%22shippingCharges%22%3A0%2C%22handlingCharges%22%3A0%2C%22subtotal%22%3A94752%2C%22excise%22%3A1610.7839999999999%2C%22VAT%22%3A0%2C%22threePLCharges%22%3A170%7D%2C%22Finance%22%3A%7B%22sellerTotal%22%3A96362.784%2C%22serviceTaxOnTsfFromSeller%22%3A14.1%2C%22tsfFromSeller%22%3A10%2C%22netPayableTo3PL%22%3A170%2C%22netPayableToSeller%22%3A96338.684%2C%22threePL%22%3A%5B%7B%22netPayable%22%3A170%2C%22name%22%3A%22Porter%22%7D%5D%2C%22serviceTaxOnMarginFromSeller%22%3A0%2C%22totalServiceTaxFromSeller%22%3A14.1%2C%22marginFromSeller%22%3A0%7D%2C%22Information%22%3A%7B%22sellerServiceTaxBasis%22%3A%22Value%22%2C%22sellerServiceTax%22%3A14.1%7D%7D%5D%2C%22gatewayPaymentMode%22%3A%22NA%22%2C%22KartChargesConsolidation%22%3A%7B%22Customer%22%3A%7B%22shippingAndHandlingAnd3PLCharges%22%3A170%2C%22shippingCharges%22%3A0%2C%22handlingCharges%22%3A0%2C%22grossTotal%22%3A96647.284%2C%22excise%22%3A1610.7839999999999%2C%22VAT%22%3A0%2C%22ActualVAT%22%3A0%2C%22ActualSubtotal%22%3A94752%2C%22subtotalWithVAT%22%3A94752%2C%22threePLCharges%22%3A170%2C%22serviceTaxOnConvenienceFee%22%3A14.5%2C%22shippingAndHandlingCharges%22%3A0%2C%22total%22%3A96532.784%2C%22convenienceFee%22%3A100%2C%22customerGatewayCharges%22%3A%5B%7B%22gatewayPaymentMode%22%3A%22CreditCard%22%2C%22gatewayChargesOnTotal%22%3A1641.0573279999999%2C%22grossTotalWithGatewayCharges%22%3A98288.341328%7D%2C%7B%22gatewayPaymentMode%22%3A%22DebitCard%22%2C%22gatewayChargesOnTotal%22%3A3089.049088%2C%22grossTotalWithGatewayCharges%22%3A99736.333088%7D%2C%7B%22gatewayPaymentMode%22%3A%22NetBanking%22%2C%22gatewayChargesOnTotal%22%3A1158.393408%2C%22grossTotalWithGatewayCharges%22%3A97805.677408%7D%2C%7B%22gatewayPaymentMode%22%3A%22PayUMoney%22%2C%22gatewayChargesOnTotal%22%3A2123.7212480000003%2C%22grossTotalWithGatewayCharges%22%3A98771.005248%7D%5D%7D%2C%22Finance%22%3A%7B%22sellerTotal%22%3A96362.784%2C%22serviceTaxOnTsfFromSeller%22%3A14.1%2C%22tsfFromSeller%22%3A10%2C%22netPayableTo3PL%22%3A170%2C%22netPayableToSeller%22%3A96338.684%2C%22threePL%22%3A%5B%7B%22netPayable%22%3A170%2C%22name%22%3A%22Porter%22%7D%5D%2C%22serviceTaxOnMarginFromSeller%22%3A0%2C%22lossOnGatewayCharges%22%3A%5B%7B%22gatewayPaymentMode%22%3A%22CreditCard%22%2C%22lossOnGatewayCharges%22%3A1.9465000000000001%7D%2C%7B%22gatewayPaymentMode%22%3A%22DebitCard%22%2C%22lossOnGatewayCharges%22%3A3.664%7D%2C%7B%22gatewayPaymentMode%22%3A%22NetBanking%22%2C%22lossOnGatewayCharges%22%3A1.374%7D%2C%7B%22gatewayPaymentMode%22%3A%22PayUMoney%22%2C%22lossOnGatewayCharges%22%3A2.519%7D%5D%2C%22totalServiceTaxFromSeller%22%3A14.1%2C%22marginFromSeller%22%3A0%7D%2C%22Information%22%3A%7B%22customerServiceTax%22%3A14.5%2C%22customerServiceTaxBasis%22%3A%22Percentage%22%2C%22customerGatewayChargesInfo%22%3A%5B%7B%22gatewayPaymentMode%22%3A%22CreditCard%22%2C%22customerGatewayChargesBasis%22%3A%22Percentage%22%2C%22customerGatewayCharges%22%3A1.7%7D%2C%7B%22gatewayPaymentMode%22%3A%22DebitCard%22%2C%22customerGatewayChargesBasis%22%3A%22Percentage%22%2C%22customerGatewayCharges%22%3A3.2%7D%2C%7B%22gatewayPaymentMode%22%3A%22NetBanking%22%2C%22customerGatewayChargesBasis%22%3A%22Percentage%22%2C%22customerGatewayCharges%22%3A1.2%7D%2C%7B%22gatewayPaymentMode%22%3A%22PayUMoney%22%2C%22customerGatewayChargesBasis%22%3A%22Percentage%22%2C%22customerGatewayCharges%22%3A2.2%7D%5D%7D%7D%2C%22ShippingChargesSummary%22%3A%5B%7B%22sellerDeliverableSkus%22%3A%5B%5D%2C%22sellerId%22%3A%22S10019%22%2C%22sellerHandlingCost%22%3A0%2C%22sellerShippingCost%22%3A0%2C%223PLInfo%22%3A%5B%7B%223PLShippingCost%22%3A170%2C%223PLDeliverableSkus%22%3A%5B%22SKU0111%22%5D%2C%223PLName%22%3A%22Porter%22%7D%5D%7D%5D%7D%0A%7D";

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

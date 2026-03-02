package com.bkap.dto;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.bkap.entity.HotDeal;

public class HotDealViewModel {
	private final HotDeal deal;
	private final static NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

	public HotDealViewModel(HotDeal deal) {
		this.deal = deal;
	}

	public String getFormattedOriginalPrice() {
		return CURRENCY_FORMAT.format(deal.getProduct().getPrice()) + "₫";
	}

	public String getFormattedDiscountedPrice() {
		double discounted = deal.getProduct().getPrice() * (100 - deal.getDiscountPercent()) / 100.0;
		return CURRENCY_FORMAT.format(discounted) + "₫";
	}

	public HotDeal getDeal() {
		return deal;
	}

	public String getTitle() {
		return deal.getTitle();
	}

	public int getDiscountPercent() {
		return deal.getDiscountPercent();
	}

	public String getProductName() {
		return deal.getProduct().getName();
	}

	public String getProductImage() {
		return deal.getProduct().getImage();
	}

	public String getFormattedEndDate() {
		if (deal.getEndDate() == null)
			return "";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		return deal.getEndDate().format(formatter);
	}

}

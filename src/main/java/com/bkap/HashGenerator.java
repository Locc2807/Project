package com.bkap;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
	 public static void main(String[] args) {
	        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	        //System.out.println("Admin: " + encoder.encode("123456"));
	        
	        System.out.println("User1: " + encoder.encode("1234567"));
	        System.out.println(encoder);
	    }
}

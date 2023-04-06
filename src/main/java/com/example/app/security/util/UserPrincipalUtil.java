package com.example.app.security.util;

import com.example.app.security.SimpleUserPrinciple;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserPrincipalUtil {

  public static SimpleUserPrinciple extractUserPrinciple() {
    return (SimpleUserPrinciple) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
  }

  public static String extractUserEmail(){
    SimpleUserPrinciple userPrinciple = extractUserPrinciple();

    return userPrinciple.getUsername();
  }


}

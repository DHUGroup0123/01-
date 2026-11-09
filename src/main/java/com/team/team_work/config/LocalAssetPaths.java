package com.team.team_work.config;
import org.springframework.stereotype.Component;
@Component("assetPaths")
public class LocalAssetPaths {
 public String image(String value){if(value!=null&&(value.startsWith("/upload/")||value.startsWith("/goods-img/")||value.startsWith("/images/"))&&!value.contains(".."))return value;return "/images/01-product.svg";}
}

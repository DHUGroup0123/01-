package com.team.team_work.memberB.admin.service;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.*;
import java.nio.file.*;
@Configuration
@PropertySource("classpath:memberB/memberb.properties")
public class MemberBConfiguration implements WebMvcConfigurer {
 private final Path uploadDir;
 public MemberBConfiguration(@Value("${memberb.upload-dir:./member-b-uploads}")String directory){uploadDir=Path.of(directory).toAbsolutePath().normalize();}
 @Override public void addResourceHandlers(ResourceHandlerRegistry registry){registry.addResourceHandler("/member-b/media/**").addResourceLocations(uploadDir.toUri().toString()+"/").setCachePeriod(31536000);}
}

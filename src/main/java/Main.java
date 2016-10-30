
/**
 * 
 */

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class Main {

	private static final int PORT=8080;
	private static final int maxThreads = 50;
	private static final int minThreads = 2;
	private static final int timeOutMillis = 30000;
	
	private static final String JETTY_MULTIPART_CONFIG = "org.eclipse.jetty.multipartConfig";
	private static final String IMAGE_NAME = "imageName";
	private static final long maxFileSize=100000000;
	private static final long maxRequestSize=100000000 ;
	private static final int fileSizeThreshold = 1024;
	private static final String baseUri="http://localhost:8080/images/";

	private static final String baseLocation="C:\\Users\\braj.kishore\\Pictures";
	private static final String tmpLocation="C:\\Users\\braj.kishore\\Pictures";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		Spark.port(PORT);
		Spark.threadPool(maxThreads, minThreads, timeOutMillis);
		get("/", (req, res) -> "Welcome to Image Processing App");
		
		get("/images/:imageName", new Route() {
			
			@Override
			public Object handle(Request request, Response response) throws Exception {
				// TODO Auto-generated method stub
				 return readImage(request, response);
			}
		});
		
		post("/images", new Route() {

			@Override
			public Object handle(Request request, Response response) throws Exception {
				// TODO Auto-generated method stub
								
				String serverFileName="";
				try {
					response.status(201);					
					serverFileName=writeImage(request);	
					response.status(201);
					response.body(baseUri+serverFileName);
				} catch (Exception e) {
					// TODO: handle exception
					response.status(500);
					response.body(e.getMessage());					
				}
				
				return response.body();
			}
		});
	}

	
	private static Object readImage(Request request, Response response) {
	   
		try {
			
			if(request.params(IMAGE_NAME)!=null){
				
				String fileName=request.params(IMAGE_NAME);
				System.out.println("Looking for "+fileName);
				Path path=Paths.get(baseLocation+"\\"+fileName);
				if(!Files.exists(path)){
					response.status(404);
					return "No such an image";
				}
				response.raw().setContentType("application/octet-stream");
				response.raw().setHeader("Content-Disposition","attachment; filename="+path.getFileName());				
				 Files.copy(path,response.raw().getOutputStream());
				
				 response.status(200);
				 return "Successfully Sent";				 
			}
			else{
				response.status(404);
				return "No such an image";
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			response.status(500);
			return e.getMessage();
		}
		
	}	   
	    
	private static String writeImage(Request request) throws IOException, ServletException {
		
		String submittedFileName="";
		String fileName="";
		MultipartConfigElement multipartConfigElement=null;
			 multipartConfigElement = new MultipartConfigElement(tmpLocation,
					 maxFileSize, maxRequestSize, fileSizeThreshold);
			
			request.raw().setAttribute(JETTY_MULTIPART_CONFIG,
				     multipartConfigElement);
			
			Part part = request.raw().getPart(IMAGE_NAME);	
			submittedFileName=part.getSubmittedFileName();
			fileName=getUnqFileName(submittedFileName);
			Path out=Paths.get(baseLocation+"\\" + fileName);
			
		
			try (final InputStream in = part.getInputStream()) {				
				   Files.copy(in, out,StandardCopyOption.REPLACE_EXISTING);				   
				   part.delete();
				}
						
			multipartConfigElement = null;
			part = null;			
		
		
		return fileName;
	}
	
	private static String getUnqFileName(String originalFileName){
		
		int indx=originalFileName.lastIndexOf(".");
		String ext=originalFileName.substring(indx).trim();
		return UUID.randomUUID().toString()+ext;
		
	}
	
}

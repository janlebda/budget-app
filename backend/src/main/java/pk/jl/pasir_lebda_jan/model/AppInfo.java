package pk.jl.pasir_lebda_jan.model;

public class AppInfo {
    private String appName;
    private String version;
    private String message;

    public AppInfo(String appName, String version, String message) {
        this.appName = appName;
        this.version = version;
        this.message = message;
    }
    public String getAppName() { return appName; }
    public String getVersion() { return version; }
    public String getMessage() { return message; }
 
    
}

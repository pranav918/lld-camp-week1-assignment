public interface INotification { 
    public boolean isUserEligibleForNotification(User user);
    public void sendNotification(User user); 
} 

public interface IMailGenerator {
    public String generateTemplate();
    public String generateBody();
}

class MailGenerater implements IMailGenerator{
    @Override
    public String generateTemplate() {
        // Template generation logic will go here currently returning hard coded string
        return  "welcome";
    }
    public String generateBody() {
        // Body generation logic will go here currently returning hard coded string
        return  "Welcome!";
    }
}

class SmtpMailer implements INotification { 
    private final IMailGenerator _mailGenerater;

    SmtpMailer(IMailGenerator mailGenerater) {
        _mailGenerater = mailGenerater;
    }

    @Override public boolean isUserEligibleForNotification(User user) { 
        if (user.email == null || user.email.isEmpty()) 
            return false;
        return true;
    } 

    @Override public void sendNotification(User user) { 
        String template = _mailGenerater.generateTemplate();
        String body = _mailGenerater.generateBody();

        System.out.println("[SMTP] template=" + template + " to=" + user.email + " body=" + body); 
    } 
} 

public interface IOtpGenerator {
    public String generateOTP();
}

class OtpGenerater implements IOtpGenerator{
    @Override
    public String generateOTP() {
        // Otp generation logic will go here currently returning hard coded string
        return  "123456";
    }
}

class TwilioClient implements INotification { 
    private final IOtpGenerator _otpGenerater;

    TwilioClient(IOtpGenerator otpGenerater) {
        _otpGenerater = otpGenerater;
    }

    @Override public boolean isUserEligibleForNotification(User user) { 
        if (user.phone == null || user.email.isEmpty()) 
            return false;
        return true;
    } 

    @Override public void sendNotification(User user) { 
        String otp = _otpGenerater.generateOTP();
        System.out.println("[Twilio] OTP " + otp + " -> " + user.phone); 
    } 
}

class User {
    String email;
    String phone;
    User(String email, String phone) {
     this.email = email; this.phone = phone;
    }
}

public interface IUserValidation {
    public boolean validUser(User user);
}

class UserValidationService implements IUserValidation {
    @Override
    public boolean validUser(User user) {
        if (user.email == null || user.email.isEmpty()) 
            return false;
        return true;
    }
}

public interface IUserRegistration {
    public void registerUser(User user);
}

class RegistrationService implements IUserRegistration {
    @Override
    public void registerUser(User user) {
         // If valid user Save to DB
    }
}

public interface INotificationChannel {
    public void sendNotificationToSupportedChannels(User u);
}

class NotificationChannel implements INotificationChannel {

    private final List<INotification> _supportedChannels;

    NotificationChannel(List<INotification> supportedChannels) {
        _supportedChannels = supportedChannels
    }
    @Override
    public void sendNotificationToSupportedChannels(User u) {
        for (INotification supportedChannel : _supportedChannels) {
            if(supportedChannel.isUserEligibleForNotification(u))
                supportedChannel.sendNotification(u);
        }
    }
}

class SignUpService {

    private final IUserValidation _userValidationService;
    private final IUserRegistration _registrationService;
    private final INotificationChannel _notificationChannel;
    

    SignUpService(IUserValidation userValidationService, IUserRegistration registrationService, INotificationChannel notificationChannel) {
            _userValidationService = userValidationService;
            _registrationService = registrationService;
            _notificationChannel = notificationChannel;
    }

    public boolean signUp(User u){
        if(_userValidationService.validUser(u)) {
            _registrationService.registerUser(u);
            _notificationChannel.sendNotificationToSupportedChannels(u);
            return true;
        }

        return false;
    }
}

public class NotifyDIPOCP {
    public static void main(String[] args) {
        List<INotification> supportedChannels = new ArrayList<INotification>();
         supportedChannels.add(new SmtpMailer(new MailGenerater()));
         supportedChannels.add(new TwilioClient(new OtpGenerater()));
        SignUpService svc = new SignUpService(new UserValidationService(), new RegistrationService(), new NotificationChannel (supportedChannels));
        svc.signUp(new User("user@example.com", "+15550001111"));
    }
}

public class FormData {
    private String Sender;
    private String Reciever;
    private String Subject;
    private String Message;

    public FormData(String Params) {
        if (isValidParams(Params))
        {
            mapParams(Params);
        }
        else
        {
            throw new IllegalArgumentException("invalid POST parameters");
        }
    }

    private boolean isValidParams(String params) {
        // later
        return true;
    }

    private void mapParams(String params) {
        String[]  requestedParams = params.split("&");
        this.Sender = requestedParams[0].split("=")[1];
        this.Reciever = requestedParams[1].split("=")[1];
        this.Subject = requestedParams[2].split("=")[1];
        this.Message = requestedParams[3].split("=")[1];
    }

    @Override
    public String toString() {
        return "Sender: " + this.Sender + "\r\n" + "Receiver: " + this.Reciever + "\r\n" + "Subject: " + this.Subject + "\r\n" + "Message: " + this.Message + "\r\n";
    }
}

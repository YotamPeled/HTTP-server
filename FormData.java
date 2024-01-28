public class FormData {
    private String Sender;
    private String Reciever;
    private String Subject;
    private String Message;

    public final int NUM_PARAM = 4;

    public FormData(String Params) {
        if (isValidParams(Params))
        {
            mapParams(Params);
        }
        else
        {
            throw new IllegalArgumentException("invalid parameters");
        }
    }

    private boolean isValidParams(String params) {
        boolean isValid = true;
        String[] requestedParams = params.split("&");
        String[] keys = new String[requestedParams.length];

        // Process each key
        for (int i = 0; i < requestedParams.length; i++) {
            String[] keyValue = requestedParams[i].split("=");
            keys[i] = keyValue[0]; // Store only the key
        }

        for (String key : keys) {
             isValid &= isValidParam(key);
        }

        return isValid && keys.length == this.NUM_PARAM;
    }

    private boolean isValidParam(String value)
    {
        return ("sender".equals(value) || "receiver".equals(value) || "subject".equals(value) || "message".equals(value));
    }
    private void mapParams(String params) throws IllegalArgumentException {
        String[]  requestedParams = params.split("&");
        for (String value : requestedParams) {
            if (value.split("=").length <= 1)
            {
                throw new IllegalArgumentException("empty param");
            }
        }
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

/**
 * Created by 余庚鑫 on 2018/8/7
 */
public class test {
    public static void main(String[] args) {
        System.out.println("2019-13-12 17:00:00".matches("^\\d{4}[-]([0][1-9]|(1[0-2]))[-]([1-9]|([012]\\d)|(3[01]))([ \\t\\n\\x0B\\f\\r])(([0-1]{1}[0-9]{1})|([2]{1}[0-4]{1}))([:])(([0-5]{1}[0-9]{1}|[6]{1}[0]{1}))([:])((([0-5]{1}[0-9]{1}|[6]{1}[0]{1})))$"));
    }

}

module org.sinbelisk.graphicftp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.net;


    opens org.sinbelisk.graphicftp to javafx.fxml;
    exports org.sinbelisk.graphicftp;
    exports org.sinbelisk.graphicftp.services;
    opens org.sinbelisk.graphicftp.services to javafx.fxml;
}
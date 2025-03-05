module org.sinbelisk.graphicftp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.net;
    requires org.apache.logging.log4j;


    opens org.sinbelisk.graphicftp to javafx.fxml;
    exports org.sinbelisk.graphicftp;
    exports org.sinbelisk.graphicftp.services;
    opens org.sinbelisk.graphicftp.services to javafx.fxml;
    exports org.sinbelisk.graphicftp.util;
    opens org.sinbelisk.graphicftp.util to javafx.fxml;
    exports org.sinbelisk.graphicftp.controller;
    opens org.sinbelisk.graphicftp.controller to javafx.fxml;
}
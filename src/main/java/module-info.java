module com.nhs2304.demosortalgo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;

    opens com.nhs2304.demosortalgo to javafx.fxml;
    exports com.nhs2304.demosortalgo;
    exports com.nhs2304.demosortalgo.model;
    opens com.nhs2304.demosortalgo.model to javafx.fxml;
    exports com.nhs2304.demosortalgo.hello;
    opens com.nhs2304.demosortalgo.hello to javafx.fxml;
}
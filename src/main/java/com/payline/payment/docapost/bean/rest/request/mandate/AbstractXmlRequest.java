package com.payline.payment.docapost.bean.rest.request.mandate;

import com.payline.payment.docapost.bean.rest.request.Request;
import com.payline.payment.docapost.bean.rest.request.mandate.MandateCreateRequest;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * Created by Thales on 04/09/2018.
 */
public abstract class AbstractXmlRequest implements Request {

    @Override
    public String buildBody() {

        String result = StringUtils.EMPTY;

        try {

            JAXBContext context = JAXBContext.newInstance(this.getClass());

            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            StringWriter writer = new StringWriter();

            marshaller.marshal(this, writer);

            result = writer.toString();

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return result;

    }

}
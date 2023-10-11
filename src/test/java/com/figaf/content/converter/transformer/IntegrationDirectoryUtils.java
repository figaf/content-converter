package com.figaf.content.converter.transformer;

import com.figaf.integration.directory.dto.CommunicationChannel;
import com.figaf.content.converter.exception.ApplicationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.xml.transform.StringSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.nio.charset.StandardCharsets;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntegrationDirectoryUtils {

    public static CommunicationChannel deserializeCommunicationChannel(byte[] payload) {
        try {
            StringSource stringSource = new StringSource(new String(payload, StandardCharsets.UTF_8));
            return createUnmarshaller(CommunicationChannel.class).unmarshal(stringSource, CommunicationChannel.class).getValue();
        } catch (JAXBException ex) {
            log.error("Error occurred while deserializing Communication Channel object: " + ex.getMessage(), ex);
            throw new ApplicationException("Error occurred while deserializing Communication Channel object: " + ex.getMessage(), ex);
        }
    }

    private static Unmarshaller createUnmarshaller(Class<?> type) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(type);
        return jc.createUnmarshaller();
    }
}

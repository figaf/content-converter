package com.figaf.content.converter.directory;

import com.figaf.content.converter.directory.dto.CommunicationChannel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntegrationDirectoryUtils {

    public static CommunicationChannel deserializeCommunicationChannel(byte[] payload) {
        try (InputStream payloadBais = new ByteArrayInputStream(payload)){
            return createUnmarshaller(CommunicationChannel.class).unmarshal(new StreamSource(payloadBais), CommunicationChannel.class).getValue();
        } catch (Exception ex) {
            log.error("Error occurred while deserializing Communication Channel object: " + ex.getMessage(), ex);
            throw new IllegalArgumentException("Error occurred while deserializing Communication Channel object: " + ex.getMessage(), ex);
        }
    }

    public static String normalize(String str) {
        return (StringUtils.isNoneBlank(str)) ? str : null;
    }

    private static Unmarshaller createUnmarshaller(Class<?> type) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(type);
        return jc.createUnmarshaller();
    }

}

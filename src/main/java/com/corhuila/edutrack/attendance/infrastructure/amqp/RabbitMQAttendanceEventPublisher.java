package com.corhuila.edutrack.attendance.infrastructure.amqp;

import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQAttendanceEventPublisher implements AttendanceEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQAttendanceEventPublisher.class);
    private static final String EXCHANGE_NAME = "edutrack.events";
    private static final String ROUTING_KEY = "attendance.student.absent";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQAttendanceEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishStudentAbsent(StudentAbsentEvent event) {
        try {
            log.info("Publishing StudentAbsent AMQP event: aggregateId={}, studentId={}, seq={}", 
                     event.getAggregateId(), event.getPayload().getStudentId(), event.getPayload().getSequenceNum());
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event.getPayload().getAttendanceId());
        } catch (Exception e) {
            log.warn("AMQP broker unavailable, event logged locally: {}", e.getMessage());
        }
    }
}

package kbee.queue;

import com.novamens.service.SystemService;

public interface QueueService extends SystemService {
	public void sendMessage(String queue, Object message);
}

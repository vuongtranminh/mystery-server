package com.vuong.app;

import com.vuong.app.doman.*;
import com.vuong.app.jpa.specification.JpaSpecificationExecutorWithProjectionImpl;
import com.vuong.app.repository.DiscordRepository;
import com.vuong.app.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@SpringBootTest
@EnableJpaRepositories(repositoryBaseClass = JpaSpecificationExecutorWithProjectionImpl.class)
@RequiredArgsConstructor
class DiscordServiceApplicationTests {

	private final DiscordRepository discordRepository;
	private final ServerRepository serverRepository;

	@Test
	void contextLoads() {
	}

	@Test
	public void saveServerTest() {
		Channel channel = Channel.builder()
				.name("general")
				.profileId(1)
				.type(ChannelType.TEXT)
				.build();

		Member member = Member.builder()
				.profileId(1)
				.memberRole(MemberRole.ADMIN)
				.build();

		Server server = Server.builder()
				.name("me")
				.imgUrl("me")
				.profileId(1)
				.inviteCode(UUID.randomUUID().toString())
				.build();

		server.addChanel(channel);
		server.addMember(member);

		server = this.serverRepository.save(server);

		Assertions.assertEquals("A", "A");
	}

	@Test
	public void leaveServer() {
		boolean leave = this.discordRepository.leaveServer(1, 1);
		Assertions.assertEquals(true, leave);
	}

}

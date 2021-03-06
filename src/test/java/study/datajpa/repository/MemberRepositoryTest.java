package study.datajpa.repository;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        // 단건 조회
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);

    }

    @Test
    public void testQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsername() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s :usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);


        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> members = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : members) {
            System.out.println(member);
        }
    }

    @Test
    public void returnType() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        Member m3 = new Member("CCC", 30);
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);

        List<Member> aaa = memberRepository.findListByUsername("AAA");
        Member bbb = memberRepository.findMemberByUsername("BBB");
        Optional<Member> ccc = memberRepository.findOptionalByUsername("CCC");

        System.out.println("optional ccc = " + ccc);

        List<Member> aaa2 = memberRepository.findListByUsername("fdjk"); // 검색결과가 없으면 널을 반환하는 것이  아닌 empty(=0)를 반환
        System.out.println("result = " + aaa2.size());

        Member bbb2 = memberRepository.findMemberByUsername("fjdk"); // 검색결과가 없으면 널을 반환
        System.out.println("result = " + bbb2);

        Optional<Member> ccc2 = memberRepository.findOptionalByUsername("fjdk"); // 검색결과가 없으면 Optional.empty 반환
        System.out.println("result = " + ccc2);

    }

    @Test
    public void paging() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findPageByAge(age, pageRequest);

        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(totalElements).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

        Slice<Member> page2 = memberRepository.findSliceByAge(age, pageRequest);

        List<Member> content2 = page2.getContent();

        assertThat(content2.size()).isEqualTo(3);
        assertThat(page2.getNumber()).isEqualTo(0);
        assertThat(page2.isFirst()).isTrue();
        assertThat(page2.hasNext()).isTrue();

    }

    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20);

        // @Modifying(clearAutomatically = true) 처리를 통해 아래 주석 처리 가능
        // em.flush();
        // em.clear();

        Member memeber5 = memberRepository.findMemberByUsername("member5");

        System.out.println("member5 = " + memeber5);

        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {

        // given
        // member1 -> teamA
        // member2 -> teamB

        Team TeamA = new Team("teamA");
        Team TeamB = new Team("teamB");

        teamRepository.save(TeamA);
        teamRepository.save(TeamB);

        Member member1 = new Member("member1", 10, TeamA);
        Member member3 = new Member("member2", 10, TeamB);
        Member member4 = new Member("member1", 10, TeamA);

        memberRepository.save(member1);
        memberRepository.save(member3);
        memberRepository.save(member4);

        em.flush();
        em.clear();

        // when N + 1
        // select Member 1
        List<Member> members = memberRepository.findAll(); // override
        List<Member> members2 = memberRepository.findMemberFetchJoin();
        List<Member> members3 = memberRepository.findEntityGraphByUsername("member1");

        for (Member member : members) {
            System.out.println("member.name = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass()); // 가짜 Team 객체 반환
            System.out.println("member.teamName = " + member.getTeam().getName() ); // 실제 Team 객체 반환
        }

        for (Member member : members2) {
            System.out.println("member2.name = " + member.getUsername());
            System.out.println("member2.teamClass = " + member.getTeam().getClass());
            System.out.println("member2.teamName = " + member.getTeam().getName() );
        }

        for (Member member : members3) {
            System.out.println("member3.name = " + member.getUsername());
            System.out.println("member3.teamClass = " + member.getTeam().getClass());
            System.out.println("member3.teamName = " + member.getTeam().getName() );
        }
    }

}

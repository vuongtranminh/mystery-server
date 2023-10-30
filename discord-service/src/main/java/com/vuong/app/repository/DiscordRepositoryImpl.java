package com.vuong.app.repository;

import com.vuong.app.doman.Member;
import com.vuong.app.doman.Member_;
import com.vuong.app.doman.Server_;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.Optional;

@Slf4j
public class DiscordRepositoryImpl implements DiscordRepository {
    @PersistenceContext
    private EntityManager em;

//    @Override
//    public boolean leaveServer(Integer serverId, Integer profileId) {
//        CriteriaBuilder cb = this.em.getCriteriaBuilder();
//        CriteriaQuery<Tuple> q = cb.createTupleQuery();
//
//        Root root = q.from(Member.class);
//
//        Join<Object, Object> member = (Join<Object, Object>) root.fetch(Member_.SERVER);
//
//        Predicate eqServerId = cb.equal(member.get(Server_.SERVER_ID), serverId);
//        Predicate notEqProfileId = cb.notEqual(member.get(Server_.PROFILE_ID), profileId);
//
//        Predicate predicate = cb.and(eqServerId, notEqProfileId);
//
//        q.where(predicate);
//
//        q.multiselect(member.get(Member_.MEMBER_ID).alias("memberId"));
//
//        Tuple memberIdTuple = em.createQuery(q).getSingleResult();
//
//        return (Integer) memberIdTuple.get("memberId");
//    }

    @Override
    public boolean leaveServer(Integer serverId, Integer profileId) {
        try {
            em.getTransaction().begin();

            CriteriaBuilder cb = this.em.getCriteriaBuilder();
            CriteriaQuery<Tuple> q = cb.createTupleQuery();

            Root root = q.from(Member.class);

            Join<Object, Object> member = (Join<Object, Object>) root.fetch(Member_.SERVER);

            Predicate eqServerId = cb.equal(member.get(Server_.SERVER_ID), serverId);
            Predicate notEqProfileId = cb.notEqual(member.get(Server_.PROFILE_ID), profileId);

            Predicate predicate = cb.and(eqServerId, notEqProfileId);

            q.where(predicate);

            q.multiselect(member.get(Member_.MEMBER_ID).alias("memberId"));

//            Tuple memberIdTuple = em.createQuery(q).getSingleResult();
//
//            Integer memberId = (Integer) memberIdTuple.get("memberId");

            // delete
            CriteriaDelete<Member> delete = cb.createCriteriaDelete(Member.class);

            // set the root class
            Root e = delete.from(Member.class);

            // set where clause
            delete.where(cb.equal(e.get(Member_.MEMBER_ID), q.multiselect(member.get(Member_.MEMBER_ID).alias("memberId"))));

            // perform update
            this.em.createQuery(delete).executeUpdate();

            em.getTransaction().commit();
            em.close();

            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return false;
    }
}

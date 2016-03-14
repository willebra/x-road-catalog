package fi.vrk.xroad.catalog.persistence;

import fi.vrk.xroad.catalog.persistence.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Set;

public interface MemberRepository extends CrudRepository<Member, Long> {

    @EntityGraph(value = "member.full-tree.graph", type = EntityGraph.EntityGraphType.LOAD)
    Set<Member> findAll();

    // TODO: test that query plan is as expected
    @EntityGraph(value = "member.full-tree.graph", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT m FROM Member m WHERE m.statusInfo.removed IS NULL")
    Set<Member> findAllActive();

    Set<Member> findAllChangedSince(@Param("since") Date since);

    Set<Member> findActiveChangedSince(@Param("since") Date since);

    /**
     * Returns only active items (non-deleted)
     * @param xRoadInstance
     * @param memberClass
     * @param memberCode
     * @return
     */
    @Query("SELECT m FROM Member m WHERE m.xRoadInstance = :xRoadInstance "
            + "AND m.memberClass = :memberClass "
            + "AND m.memberCode = :memberCode "
            + "AND m.statusInfo.removed IS NULL")
    Member findByNaturalKey(@Param("xRoadInstance") String xRoadInstance,
                            @Param("memberClass") String memberClass,
                            @Param("memberCode") String memberCode);

}

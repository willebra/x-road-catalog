package fi.vrk.xroad.catalog.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class MemberDataList implements Serializable {

    private static final long serialVersionUID = 4049561366368846285L;

    private LocalDateTime date;

    private List<MemberData> memberDataList;
}

/**
 * The MIT License
 * Copyright (c) 2020, Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.vrk.xroad.catalog.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString(exclude = "service")
public class OpenApi {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OPEN_API_GEN")
    @SequenceGenerator(name = "OPEN_API_GEN", sequenceName = "OPEN_API_ID_SEQ", allocationSize = 1)
    private long id;
    @ManyToOne
    @JoinColumn(name = "SERVICE_ID")
    private Service service;
    // this is not lazy loaded since hibernate would need build-time bytecode enhancement
    // this could be optimized e.g. by not mapping this data to JPA entity, and
    // fetching it directly with a native query
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 40000, nullable = false) // big enough so that autogenerated tables can fit test data
    private String data;
    @Column(nullable = false)
    private String externalId;
    @Embedded
    private StatusInfo statusInfo = new StatusInfo();

    public OpenApi() {
        // Empty constructor
    }

    /**
     * Constructor
     */
    public OpenApi(Service service, String data, String externalId) {
        this.service = service;
        this.data = data;
        this.externalId = externalId;
        statusInfo.setTimestampsForNew(LocalDateTime.now());
    }

    public void initializeExternalId() {
        externalId = System.currentTimeMillis() + "_" + UUID.randomUUID().toString();
    }
}

/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.isisaddons.module.poly.fixture.dom.modules.fixedasset;

import org.isisaddons.module.poly.fixture.dom.modules.casemgmt.CaseContent;
import org.isisaddons.module.poly.fixture.dom.modules.comms.CommunicationChannel;
import org.isisaddons.module.poly.fixture.dom.modules.comms.CommunicationChannelOwner;
import org.isisaddons.module.poly.fixture.dom.modules.comms.CommunicationChannelOwnerLink;
import org.isisaddons.module.poly.fixture.dom.modules.comms.CommunicationChannelOwnerLinks;

import java.util.List;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.VersionStrategy;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.util.ObjectContracts;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY,
         column="id")
@javax.jdo.annotations.Version(
        strategy=VersionStrategy.VERSION_NUMBER, 
        column="version")
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "find", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.isisaddons.module.poly.fixture.dom.modules.fixedasset.FixedAsset ")
})
@javax.jdo.annotations.Unique(name="FixedAsset_name_UNQ", members = {"name"})
@DomainObject(
        objectType = "FIXED_ASSET",
        bounded = true
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
)
public class FixedAsset implements CommunicationChannelOwner, CaseContent, Comparable<FixedAsset> {

    //region > identification
    public TranslatableString title() {
        return TranslatableString.tr("{name}", "name", getName());
    }

    //endregion

    //region > name (property)

    private String name;

    @Column(allowsNull="false", length = 40)
    @Title(sequence="1")
    @Property(
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            // contributed 'title' property (from CaseContentContributions) is shown instead on tables
            hidden = Where.ALL_TABLES
    )
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // endregion

    //region > updateName (action)

    public FixedAsset updateName(
            @Parameter(maxLength = 40)
            @ParameterLayout(named = "New name")
            final String name) {
        setName(name);
        return this;
    }

    public String default0UpdateName() {
        return getName();
    }

    //endregion


    //region > createCommunicationChannel (contributed action)
    @ActionLayout(
            contributed = Contributed.AS_ACTION
    )
    @MemberOrder(name="CommunicationChannel",sequence = "3")
    public CommunicationChannelOwner createCommunicationChannel(
            @ParameterLayout(named = "Details")
            final String details) {

        final CommunicationChannel communicationChannel = container.newTransientInstance(CommunicationChannel.class);
        communicationChannel.setDetails(details);
        container.persist(communicationChannel);
        container.flush();

        communicationChannelOwnerLinks.createLink(communicationChannel, this);
        return this;
    }

    public String disableCreateCommunicationChannel(
            final String details) {
        return getCommunicationChannel() != null? "Already owns a communication channel": null;
    }

    public String validateCreateCommunicationChannel(final String details) {
        return details.contains("!")? "No exclamation marks allowed in details": null;
    }
    //endregion

    //region > deleteCommunicationChannel (contributed action)
    @ActionLayout(
            contributed = Contributed.AS_ACTION
    )
    @MemberOrder(name="CommunicationChannel", sequence = "4")
    public CommunicationChannelOwner deleteCommunicationChannel() {

        final CommunicationChannelOwnerLink ownerLink = getCommunicationChannelOwnerLink();
        final CommunicationChannel communicationChannel = getCommunicationChannel();

        container.removeIfNotAlready(ownerLink);
        container.removeIfNotAlready(communicationChannel);

        return this;
    }

    public String disableDeleteCommunicationChannel() {
        return getCommunicationChannelOwnerLink() == null? "Does not own a communication channel": null;
    }
    //endregion


    //region > communicationChannel (derived property)
    public CommunicationChannel getCommunicationChannel() {
        final CommunicationChannelOwnerLink ownerLink = getCommunicationChannelOwnerLink();
        return ownerLink != null? ownerLink.getCommunicationChannel(): null;
    }
    private CommunicationChannelOwnerLink getCommunicationChannelOwnerLink() {
        final List<CommunicationChannelOwnerLink> link = communicationChannelOwnerLinks.findByOwner(this);
        return link.size() == 1? link.get(0): null;
    }
    //endregion

    //region > compareTo

    @Override
    public int compareTo(final FixedAsset other) {
        return ObjectContracts.compare(this, other, "name");
    }

    //endregion

    //region > injected services

    @javax.inject.Inject
    DomainObjectContainer container;

    @javax.inject.Inject
    CommunicationChannelOwnerLinks communicationChannelOwnerLinks;


    //endregion

}

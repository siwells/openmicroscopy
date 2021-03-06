/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
#include <boost_fixture.h>
#include <omero/model/AcquisitionModeI.h>
#include <omero/model/ChannelI.h>
#include <omero/model/DimensionOrderI.h>
#include <omero/model/ImageI.h>
#include <omero/model/LogicalChannelI.h>
#include <omero/model/PhotometricInterpretationI.h>
#include <omero/model/PixelsI.h>
#include <omero/model/PixelsTypeI.h>
#include <omero/model/PlaneInfoI.h>
#include <omero/model/StatsInfoI.h>

using namespace omero::api;
using namespace omero::model;
using namespace omero::rtypes;

BOOST_AUTO_TEST_CASE( SavingPixels )
{
    Fixture f;

    const omero::client_ptr client = f.login();
    ServiceFactoryPrx sf = client->getSession();

    PixelsIPtr pix = f.pixels();

    // At this point trying to save throws a ValidationException
    try {
        sf->getUpdateService()->saveObject(pix);
        BOOST_ERROR("Should fail");
    } catch (const omero::ValidationException& ve) {
        // ok
    }

    ImagePtr i = new_ImageI();
    i->addPixels( pix );
    i->setName( rstring("test1") );

    try {
    	sf->getUpdateService()->saveObject(i);
    } catch (const omero::ValidationException& ve) {
        // ok
        BOOST_ERROR(ve.serverStackTrace);
    }

}

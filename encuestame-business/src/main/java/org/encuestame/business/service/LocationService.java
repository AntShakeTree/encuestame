/*
 ************************************************************************************
 * Copyright (C) 2001-2010 encuestame: system online surveys Copyright (C) 2009
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.encuestame.business.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.encuestame.business.service.imp.ILocationService;
import org.encuestame.core.exception.EnMeDomainNotFoundException;
import org.encuestame.core.exception.EnMeExpcetion;
import org.encuestame.core.util.ConvertDomainBean;
import org.encuestame.persistence.domain.GeoPoint;
import org.encuestame.persistence.domain.GeoFolder;
import org.encuestame.persistence.domain.CatLocationType;
import org.encuestame.persistence.domain.LocationFolderType;
import org.encuestame.persistence.domain.Status;
import org.encuestame.persistence.domain.notifications.NotificationEnum;
import org.encuestame.utils.web.UnitLocationBean;
import org.encuestame.utils.web.UnitLocationFolder;
import org.encuestame.utils.web.UnitLocationTypeBean;
import org.springframework.stereotype.Service;

/**
 * Location Service.
 * @author Picado, Juan juanATencuestame.org
 * @since May 15, 2010 8:17:15 PM
 * @version $Id: $
 */
@Service
public class LocationService  extends AbstractBaseService implements ILocationService{

    /** Log. **/
    private Logger log = Logger.getLogger(this.getClass());

    /**
     * Create Cat LocationType.
     * @param locatTypeBean {@link UnitLocationTypeBean}
     * @return locatTypeBean type bean
     * @throws EnMeExpcetion exception
     */
    public UnitLocationTypeBean createCatLocationType(
            final UnitLocationTypeBean locatTypeBean, final String username) throws EnMeExpcetion {
        if (locatTypeBean != null) {
            try {
                final CatLocationType locationTypeDomain = new CatLocationType();
                locationTypeDomain.setLocationTypeDescription(locatTypeBean
                        .getLocTypeDesc());
                locationTypeDomain.setLocationTypeLevel(locatTypeBean
                        .getLevel());
                locationTypeDomain.setUsers(getUser(username).getSecUser());
                getCatLocationTypeDao().saveOrUpdate(locationTypeDomain);
                locatTypeBean.setIdLocType(locationTypeDomain
                        .getLocationTypeId());
            } catch (Exception e) {
                throw new EnMeExpcetion(e);
            }
            return locatTypeBean;
        } else {
            throw new EnMeExpcetion("Cat Location Type is null");
        }
    }

    /**
     * Update Cat Location.
     * @param locationBean locationBean
     * @param username username
     * @throws EnMeExpcetion EnMeExpcetion
     */
    public void updateCatLocation(final UnitLocationBean locationBean, final String username) throws EnMeExpcetion{
       final GeoPoint catLocation =  getLocation(locationBean.getId(), username);
        if (catLocation!=null){
            catLocation.setLocationStatus(Status.valueOf(locationBean.getStatus()));
            catLocation.setLocationDescription(locationBean.getName());
            catLocation.setLocationLatitude(locationBean.getLat());
            catLocation.setLocationLongitude(locationBean.getLng());
            getCatLocationDao().saveOrUpdate(catLocation);
        }else{
            throw new EnMeExpcetion("location not found");
        }
   }


    /**
     * Update Cat Location Type.
     * @param locationTypeBean locationTypeBean
     * @throws EnMeExpcetion EnMeExpcetion
     */
    public void updateCatLocationType(final UnitLocationTypeBean locationTypeBean) throws EnMeExpcetion{
        final CatLocationType catLocationType = getCatLocationTypeDao().getLocationById(locationTypeBean.getIdLocType());
        if (catLocationType!=null){
            catLocationType.setLocationTypeDescription(locationTypeBean.getLocTypeDesc());
            catLocationType.setLocationTypeLevel(locationTypeBean.getLevel());
            getCatLocationTypeDao().saveOrUpdate(catLocationType);
        }
        else{
            throw new EnMeDomainNotFoundException("location type not found");
        }
    }

    /**
     * Create Cat Location.
     * @param location {@link LocationBean}
     * @throws EnMeExpcetion exception
     */
    public UnitLocationBean createCatLocation(final UnitLocationBean location, final String username) throws EnMeExpcetion{
        if (location != null){
            try{
                final GeoPoint catLocationDomain = new GeoPoint();
                catLocationDomain.setLocationDescription(location.getName());
                catLocationDomain.setLocationStatus(Status.ACTIVE);
                catLocationDomain.setLocationLatitude(location.getLat());
                catLocationDomain.setSecUsers(getUser(username).getSecUser());
                catLocationDomain.setLocationLongitude(location.getLng());
                if(location.getTidtype() != null){
                    catLocationDomain.setTidtype(getCatLocationTypeDao().getLocationById(location.getTidtype()));
                }
                getCatLocationDao().saveOrUpdate(catLocationDomain);
                location.setId(catLocationDomain.getLocateId());
                createNotification(NotificationEnum.LOCATION_NEW, location.getName() +" is created.", catLocationDomain.getSecUsers());
            } catch (Exception e) {
                throw new EnMeExpcetion(e);
            }
            return location;
        } else {
            throw new EnMeExpcetion("location info not found");
        }
    }

    /**
     * Create Location Folder.
     * @param locationFolder {@link UnitLocationFolder}
     * @return {@link UnitLocationFolder}.
     * @throws EnMeDomainNotFoundException
     */
    public UnitLocationFolder createLocationFolder(final UnitLocationFolder locationFolder, final String username) throws EnMeDomainNotFoundException{
        final GeoFolder catLocationFolder = new GeoFolder();
        catLocationFolder.setFolderType(LocationFolderType.valueOf(locationFolder.getType()));
        catLocationFolder.setLocationFolderName(locationFolder.getName());
        catLocationFolder.setSecUsers(getUser(username).getSecUser());
        getCatLocationDao().saveOrUpdate(catLocationFolder);
        locationFolder.setId(catLocationFolder.getLocationFolderId());
        createNotification(NotificationEnum.LOCATION_FOLDER_NEW, "New Folder "+locationFolder.getName() +" is created.", catLocationFolder.getSecUsers());
        return locationFolder;
    }

    /**
     * Assign Location to Location Folder.
     * @param location
     */
    public void assignLocationToLocationFolder(final GeoPoint location, final GeoFolder catLocationFolder){
            location.setCatLocationFolder(catLocationFolder);
            getCatLocationDao().saveOrUpdate(location);
    }

    /**
     * Retrieve Location Folders by User.
     * @param currentName
     * @throws EnMeDomainNotFoundException
     */
    public List<UnitLocationFolder> retrieveLocationFolderByUser(final String currentUserName) throws EnMeDomainNotFoundException{
        return ConvertDomainBean.convertListToUnitLocationFolderBean(getCatLocationDao()
                                .getLocationFolders(getPrimaryUser(currentUserName)));
    }

    /**
     * Retrieve Location Sub Folders by User.
     * @param currentName
     * @throws EnMeDomainNotFoundException
     */
    public List<UnitLocationFolder> retrieveLocationSubFolderByUser(final Long locationFolderId, final String currentUserName) throws EnMeDomainNotFoundException{
        return ConvertDomainBean.convertListToUnitLocationFolderBean(getCatLocationDao()
                                .getLocationFoldersByLocationFolderId(locationFolderId, getPrimaryUser(currentUserName)));
    }

    /**
     * Retrieve Locations Folder Items by Folder Id and User Id.
     * @param locationFolderId location folder id
     * @param username username
     * @return
     * @throws EnMeDomainNotFoundException
     */
    public List<UnitLocationBean> retrieveLocationFolderItemsById(final Long locationFolderId, final String username) throws EnMeDomainNotFoundException{
        return ConvertDomainBean.convertListToUnitLocationBean(getCatLocationDao()
                                .getLocationByFolder(locationFolderId, getPrimaryUser(username)));
    }

    /**
     * Retrieve Locations Items by Username
     * @param username username
     * @return
     * @throws EnMeDomainNotFoundException
     */
    public List<UnitLocationBean> retrieveLocationItemsByUsername(final String username) throws EnMeDomainNotFoundException{
        return ConvertDomainBean.convertListToUnitLocationBean(getCatLocationDao()
                                .getLocationByUser(getPrimaryUser(username)));
    }

    /**
     * Get Location Item.
     * @param locationId location id
     * @param username username
     * @return
     * @throws EnMeDomainNotFoundException
     */
    public UnitLocationBean getLocationItem(final Long locationId, final String username) throws EnMeDomainNotFoundException{
        return ConvertDomainBean.convertLocationToBean(getLocation(locationId, username));
    }

    /**
     * Get Folder Location Detail.
     * @param folderLocationId folder location  Id.
     * @param username username
     * @return
     * @throws EnMeDomainNotFoundException
     */
    public UnitLocationFolder getFolderLocation(final Long folderLocationId, final String username) throws EnMeDomainNotFoundException{
        return ConvertDomainBean.convertCatLocationFolderDomainToBean(getCatLocationDao()
                                .getLocationFolderByIdAndUserId(folderLocationId, getPrimaryUser(username)));
    }

    /**
     * Update Location Map.
     * @param Latitude
     * @param Longitude
     * @param locationId
     * @param username
     * @throws EnMeExpcetion
     */
    public void updateLocationMap(final UnitLocationBean locationBean, final Long locationId, final String username) throws EnMeExpcetion{
        final GeoPoint location = getLocation(locationId, username);
        log.info("location map location "+location);
        if(location == null){
            throw new EnMeExpcetion("location not found");
        }
        else{
            location.setLocationAccuracy(locationBean.getAccuracy());
            location.setLocationLatitude(locationBean.getLat());
            location.setLocationAddress(locationBean.getAddress());
            location.setLocationCountryCode(locationBean.getCountryCode());
            location.setLocationCountryName(locationBean.getCountryName());
            location.setLocationLongitude(locationBean.getLng());
            getCatLocationDao().saveOrUpdate(location);
            createNotification(NotificationEnum.LOCATION_GMAP_UPDATED, "Updated to "+ locationBean.getAddress(), location.getSecUsers());
            log.info("location map updated");
        }
    }

    /**
     * Get {@link GeoPoint}.
     * @param locationId location Id
     * @param username username
     * @return
     * @throws EnMeDomainNotFoundException
     */
    private GeoPoint getLocation(final Long locationId, final String username) throws EnMeDomainNotFoundException{
        return getCatLocationDao().getLocationById(locationId, getPrimaryUser(username));
    }

    /**
     *
     * @param locationFolderId
     * @param username
     * @return
     * @throws EnMeDomainNotFoundException
     */
    private GeoFolder getLocationFolder(final Long locationFolderId, final String username) throws EnMeDomainNotFoundException{
        return getCatLocationDao().getLocationFolderByIdAndUserId(locationFolderId, getPrimaryUser(username));
    }

    /**
     * Update Location Name.
     * @param locationBean {@link UnitLocationBean}.
     * @param username username logged
     * @throws EnMeExpcetion exception
     */
    public void updateLocationName(final UnitLocationBean locationBean, final String username) throws EnMeExpcetion{
        final GeoPoint location = getLocation(locationBean.getId(), username);
        if(location == null){
            throw new EnMeExpcetion("location not found");
        }
        else{
            final String lastName = location.getLocationDescription();
            location.setLocationDescription(locationBean.getName());
            getCatLocationDao().saveOrUpdate(location);
            log.info("location name updated");
            createNotification(NotificationEnum.LOCATION_GMAP_CHANGED_NAME,
                               lastName+" is update to "+locationBean.getName(), location.getSecUsers());
        }
    }

    /**
     * Update Location Folder.
     * @param locationBean
     * @param username
     * @param typeUpdate
     * @throws EnMeExpcetion
     */
    public void updateLocationFolder(final UnitLocationFolder locationFolderBean,
            final String username, final String typeUpdate)
            throws EnMeExpcetion {
        final GeoFolder locationFolder = getLocationFolder(locationFolderBean
                .getId(), username);
        if (locationFolder == null) {
            throw new EnMeExpcetion("location folder not found");
        }
        else {
            if (typeUpdate.equals("name")) {
                log.debug("updating folder name");
                locationFolder.setLocationFolderName(locationFolderBean.getName());
            }
            getCatLocationDao().saveOrUpdate(locationFolder);
            createNotification(NotificationEnum.LOCATION_GMAP_CHANGED_NAME, "Folder name change to "
                                + locationFolderBean.getName(), locationFolder.getSecUsers());
        }
    }

    /**
     * Create Default Location Item.
     * @param locationFolder
     * @param username
     * @throws EnMeExpcetion
     */
    public void createDefaultILocationItem(final UnitLocationFolder locationFolderBean, final String username) throws EnMeExpcetion{
        log.info("createDefaultILocationItem");
        final GeoFolder locationFolder = getLocationFolder(locationFolderBean
                .getId(), username);
        log.info("createDefaultILocationItem locationFolder "+locationFolder);
        if (locationFolder == null) {
            throw new EnMeExpcetion("location folder not found");
        }
        else {
            final GeoPoint catLocation = new GeoPoint();
            catLocation.setCatLocationFolder(locationFolder);
            catLocation.setSecUsers(getUser(username).getSecUser());
            catLocation.setLocationStatus(Status.ACTIVE);
            catLocation.setLocationDescription("Default Item Name");
            getCatLocationDao().saveOrUpdate(catLocation);
            log.info("Default Location Item Created");
        }
    }

    /**
     * Delete Location Folder.
     * @param unitLocationFolder
     * @param username
     * @throws EnMeExpcetion
     */
    public void deleteLocationFolder(final UnitLocationFolder unitLocationFolder, final String username) throws EnMeExpcetion{
        final GeoFolder locationFolder = getLocationFolder(unitLocationFolder.getId(), username);
        log.info("deleteLocationFolder locationFolder "+locationFolder);
        if (locationFolder == null) {
            throw new EnMeExpcetion("location folder not found");
        }
        else {
            //TODO: we need remove items on CASCADE.
            final List<GeoPoint> itemsToDelete = getCatLocationDao()
                                    .getLocationByFolder(locationFolder.getLocationFolderId(), getPrimaryUser(username));
            for (GeoPoint catLocation : itemsToDelete) {
                 getCatLocationDao().delete(catLocation);
            }
            getCatLocationDao().delete(locationFolder);
            log.info("delete location folder");
        }
    }

    /**
     * Delete Location Item.
     * @param unitLocationBean
     * @param username
     */
    public void deleteLocationItem(final UnitLocationBean unitLocationBean, final String username) throws EnMeExpcetion{
        final GeoPoint location = getLocation(unitLocationBean.getId(), username);
        if(location == null){
            throw new EnMeExpcetion("location not found");
        }
        else{
           //TODO: Maybe we have conflict in the future if this location was used on other tables, delete on cascade
           // will not a good option, we need think how to resolve this problem.
           // A possible solution is change status to INACTIVE, and it not show on tree.
           getCatLocationDao().delete(location);
        }
    }
}

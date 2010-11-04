<?php
/**
  * This function is added to ease grouper login. It could be incorporated in the grouper client and get
  * the parameters via the $grouper array, but this way the parameters are explicitly defined
  * and more visible.
  */

function new_grouper() {
  global $grouper;
  global $uuid;
  return new grouper(
  	$grouper['host'],
  	$grouper['port'],
  	$grouper['ssl'],
  	$grouper['wsroot'],
  	$grouper['user'],
  	$grouper['password'],
  	$grouper['stem'],
  	$uuid,
  	array_key_exists('debug', $grouper) ? $grouper['debug'] : 0
  );
}

// Grouper class
class Grouper
{
    /**
     * Grouper client version
     *
     * const String
     */
    const clientVersion = 'v1_5_000';

    /**
     * Grouper Stem.
     *
     * @var String
     */
    protected $_stem;

    /**
     * Authenticated unique user id.
     *
     * @var String
     */
    protected $_uuid;

    /**
     * Grouper SimpleSoap client resource.
     *
     * @var SoapClient object
     */
    protected $_client;

    /**
     * Debug level
     * 
     * @var Integer
     */
    protected $_debug;
    
    /**
     * Grouper constructor
     *
     */
    public function __construct($host, $port, $ssl, $wsroot, $user, $password, $stem, $uuid, $debug = 0) {
	$protocol = $ssl ? "https" : "http";
	$wsdl = $protocol . '://' . urlencode($user) . ':' . urlencode($password) . "@$host:$port/$wsroot/services/GrouperService?wsdl";
	$options = array(
	  'login' => $user,
	  'password' => $password,
	  // During development and testing we set location explicitly, in production this will be handled by wsdl-endpoint
	  // and the following line should be removed!
	  'location' => $protocol . '://' . urlencode($user) . ':' . urlencode($password) . "@$host:$port/$wsroot/services/GrouperService.GrouperServiceHttpSoap12Endpoint/",
	  'features' => SOAP_SINGLE_ELEMENT_ARRAYS	# return arrays, even if single valued, important for "foreach" constructions.
	);

	try {
		$this->_client = @new SoapClient($wsdl, $options);
	} catch (Exception $e) {
		error_log($e->getMessage());
		$this->_client = FALSE;
	}

	$this->_stem = $stem;
	$this->_uuid = $uuid;
	$this->_debug = $debug;
    }

    /**
     * See if the SoapClient connection succeeded.
     *
     * @return true for success or false for failed connection
     */
    public function isConnected() {
		return $this->_client;
    }

    public function debugCall($name, $result) {
		if ($this->_debug > 0) {
			error_log($name . ' : ' . print_r($result, TRUE));
			//var_dump($result);
			//ob_start();
			//var_dump($result);
			//$r = ob_get_contents();
			//ob_end_clean();			
			//error_log($r);
		}
	}
    
    /**
     * Get info about a group.
     *
     * @param String $search search string
     *
     * @return Numbered Array of group arrays.
     */
    public function findGroups($search) {
      if ($search != "") $qft = 'FIND_BY_GROUP_NAME_APPROXIMATE';
      else $qft = 'FIND_BY_STEM_NAME';

      $args = array(
	'clientVersion' => self::clientVersion,
	'queryFilterType' => $qft,
	'groupName' => $search,
	'stemName' => $this->_stem,
	'stemNameScope' => '',
	'groupUuid' => '',
	'groupAttributeName' => '',
	'groupAttributeValue' => '',
	'groupTypeName' => '',
	'actAsSubjectId' => $this->_uuid,
	'actAsSubjectSourceId' => '',
	'actAsSubjectIdentifier' => '',
	'includeGroupDetail' => ''
      );

	try {
		$result = $this->_client->findGroupsLite($args)->return->groupResults;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('findGroupsLite', $result);
	
      $i = 0; $groups = array();
      if (is_array($result)) foreach ($result as $id => $attr) {
		$groups[$i]['id'] = $attr->name;
	$groups[$i]['name'] = $attr->displayExtension;
	$groups[$i]['description'] = $attr->description;
	$i++;
      }
      return $groups;
    }

    /**
     * Get info about a group.
     *
     * @param String $group Group Id.
     *
     * @return Array group info.
     */
    public function getGroup($group) {
      $args = array(
	'clientVersion' => self::clientVersion,
	'queryFilterType' => 'FIND_BY_GROUP_NAME_EXACT',
	'groupName' => $group,
	'stemName' => '',
	'stemNameScope' => '',
	'groupUuid' => '',
	'groupAttributeName' => '',
	'groupAttributeValue' => '',
	'groupTypeName' => '',
	'actAsSubjectId' => $this->_uuid,
	'actAsSubjectSourceId' => '',
	'actAsSubjectIdentifier' => '',
	'includeGroupDetail' => 'true'
      );

	try {
		$result = $this->_client->findGroupsLite($args)->return->groupResults;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('findGroupsLite', $result);
      
      $group = array();
      if (is_array($result)) foreach ($result as $id => $attr) {
	$group['id'] = $attr->name;
	$group['extension'] = $attr->extension;
	$group['name'] = $attr->displayExtension;
	$group['description'] = $attr->description;
	$group['created'] = $attr->detail->createTime;
	$group['creator'] = $attr->detail->createSubjectId;
	$group['modified'] = $attr->detail->modifyTime;
	$group['modifier'] = $attr->detail->modifySubjectId;
      }
      return $group;
    }


    /**
     * Get all the groups a subject belongs to.
     *
     * @param String $subject User Id.
     *
     * @return Numbered Array of group arrays.
     */
    public function getSubjectGroups($subject) {
      $args = array(
	  'clientVersion' => self::clientVersion,
	  'subjectId' => $subject,
	  'subjectSourceId' => '',
	  'subjectIdentifier' => '',
	  'memberFilter' => '',
	  'actAsSubjectId' => $this->_uuid,
	  'actAsSubjectSourceId' => '',
	  'actAsSubjectIdentifier' => '',
	  'includeGroupDetail' => 'true',
	  'includeSubjectDetail' => 'false',
	  'subjectAttributeNames' => ''         # Foreign subject attributes to retrieve
      );

	try {
		$result = $this->_client->getGroupsLite($args)->return->wsGroups;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('getGroupsLite', $result);

      $i = 0;	$groups = array();
      // getGroupsLite does /not/ support stem scoping (yet).
      // Therefor we need to filter out the groups outside our stemscope here.
      if (is_array($result)) foreach ($result as $id => $attr) {
	if (stripos($attr->name, $this->_stem) === 0) { // Only if the position of the stem === 0, this is a valid group.
	  $groups[$i]['id'] = $attr->name;
	  $groups[$i]['extension'] = $attr->extension;
	  $groups[$i]['name'] = $attr->displayExtension;
	  $groups[$i]['description'] = $attr->description;
	  $i++;
	}
      }
      return $groups;
    }

    /**
     * Get the members of a group.
     *
     * @param String $group Group Id.
     *
     * @return Numbered Array of member arrays.
     */
    public function getGroupMembers($group) {
      $args = array(
	  'clientVersion' => self::clientVersion,
	  'groupName' => $group,
	  'groupUuid' => '',
	  'memberFilter' => '',
	  'actAsSubjectId' => $this->_uuid,
	  'actAsSubjectSourceId' => '',
	  'actAsSubjectIdentifier' => '',
	  'fieldName' => '',
	  'includeGroupDetail' => 'false',
	  'includeSubjectDetail' => 'false',
      );

	try {
		$result = $this->_client->getMembersLite($args)->return->wsSubjects;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}
	
	$this->debugCall('getMembersLite', $result);
	
      $i = 0; $members = array();
      if (is_array($result)) foreach ($result as $id => $attr) {
	$members[$i]['id'] = $attr->id;
	$members[$i]['name'] = $attr->name;
	$i++;
      }
      return $members;
    }

    /**
     * Get the privileges for a group.
     *
     * @param String $group Group Id
     *
     * @return Array of privilege array per subject (key).
     */
    public function getGroupPrivileges($group) {
      $args = array(
	'clientVersion' => self::clientVersion,
	'subjectId' => '',
	'subjectSourceId' => '',
	'subjectIdentifier' => '',
	'groupName' => $group,
	'groupUuid' => '',
	'stemName' => '',
	'stemUuid' => '',
	'privilegeType' => '',
	'privilegeName' => '',
//	'actAsSubjectId' => $this->_uuid,
	'actAsSubjectId' => 'GrouperSystem', // Have to use GrouperSystem, because non-admins can't see privileges
	'actAsSubjectSourceId' => '',
	'actAsSubjectIdentifier' => '',
	'includeSubjectDetail' => 'false',
	'subjectAttributeNames' => '',
	'includeGroupDetail' => 'false'
      );

	try {
		$result = $this->_client->getGrouperPrivilegesLite($args)->return->privilegeResults;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('getGrouperPrivilegesLite', $result);

      $privs = array();
      if (is_array($result)) foreach ($result as $id => $attr) {
	$privs[$attr->wsSubject->id][] = $attr->privilegeName;
      }

      return $privs;
    }


    /**
     * Get the privileges for a member of a group.
     *
     * @param String $subject Subject Id, $group Group Id
     *
     * @return Array of privileges.
     */
    public function getSubjectGroupPrivileges($subject, $group) {
      $args = array(
	'clientVersion' => self::clientVersion,
	'subjectId' => $subject,
	'subjectSourceId' => '',
	'subjectIdentifier' => '',
	'groupName' => $group,
	'groupUuid' => '',
	'stemName' => '',
	'stemUuid' => '',
	'privilegeType' => '',
	'privilegeName' => '',
	'actAsSubjectId' => $this->_uuid,
	'actAsSubjectSourceId' => '',
	'actAsSubjectIdentifier' => '',
	'includeSubjectDetail' => 'false',
	'subjectAttributeNames' => '',
	'includeGroupDetail' => 'false'
      );

	try {
		$result = $this->_client->getGrouperPrivilegesLite($args)->return->privilegeResults;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('getGrouperPrivilegesLite', $result);	

      $privs = array();
      if (is_array($result)) foreach ($result as $id => $attr) {
	$privs[] = $attr->privilegeName;
      }

      return $privs;
    }

  /**
    * Add a new group.
    * addGroup does basic name checking and character replacement. Should be safe without pre-check.
    * Pre-check is advised though, to have better feedback in the GUI.
    *
    * @param String $group Group Id, $description Group Description
    *
    * @return new Group array on success, or -1 on failure.
    */
  public function addGroup($group, $description) {
    // Create group
    // verboden extension (ID) karakters: "<>/*' en [spatie]
    $forbidden = array('"', '<' , '>' , '/' , '\\', '*', ':');
    $groupname = str_replace($forbidden, "", $group);

    $groupid = str_replace(" ", "_", $groupname);
    $groupid = str_replace("'", "", $groupid);
    $groupid = strtolower($groupid);

    $args = array(
      'clientVersion' => self::clientVersion,
      'groupLookupUuid' => '',
      'groupLookupName' => '',
      'groupUuid' => '',
      'groupName' => $this->_stem . ":" . $groupid,
      'displayExtension' => $groupname,
      'description' => $description,
      'saveMode' => 'INSERT',
      'actAsSubjectId' => 'GrouperSystem'
      #'actAsSubjectId' =>  $this->_uuid
    );

	try {
		$result = $this->_client->groupSaveLite($args)->return;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('groupSaveLite', $result);
	
    if ($result->resultMetadata->success == 'T') {
      $new_group['id'] = $result->wsGroup->name;
      $new_group['name'] = $result->wsGroup->displayExtension;
      $new_group['description'] = $result->wsGroup->description;
    } else {
      $new_group = -1;
    }

    return $new_group;

  }

  /**
    * Update group
    * updateGroup does basic name checking and character replacement. Should be safe without pre-check.
    * Pre-check is advised though, to have better feedback in the GUI.
    *
    * @param String $group Group Id, $name Group name, $description Group description
    *
    * @return updated Group array or -1 on failure.
    */
  public function updateGroup($group, $name, $description) {
    $forbidden = array('"', '<' , '>' , '/' , '\\', '*', ':');
    $name = str_replace($forbidden, "", $name);
    $name = stripslashes($name);

    $description = stripslashes($description);

    $args = array(
      'clientVersion' => self::clientVersion,
      'groupLookupUuid' => '',
      'groupLookupName' => $group,
      'groupUuid' => '',
      'groupName' => $group,
      'displayExtension' => $name,
      'description' => $description,
      'saveMode' => 'UPDATE',
      'actAsSubjectId' => $this->_uuid
    );

	try {
		$result = $this->_client->groupSaveLite($args)->return;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('groupSaveLite', $result);

    if ($result->resultMetadata->success == 'T') {
      $new_group['id'] = $result->wsGroup->name;
      $new_group['extension'] = $result->wsGroup->extension;
      $new_group['name'] = $result->wsGroup->displayExtension;
      $new_group['description'] = $result->wsGroup->description;
    } else {
      $new_group = -1;
    }

    return $new_group;

  }


  /**
    * Delete group
    *
    * @param String $group Group Id
    *
    * @return true on success or false on failure, -1 on error.
    */
  public function deleteGroup($group) {
    $args = array(
      'clientVersion' => self::clientVersion,
      'groupName' => $group,
      'groupUuid' => '',
      'actAsSubjectId' => $this->_uuid
    );

	try {
		$result = $this->_client->groupDeleteLite($args)->return;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('groupDeleteLite', $result);

	if ($result->resultMetadata->success == 'T') {
      return TRUE;
    } else {
      return FALSE;
    }

  }

  /**
    * Add Member
    *
    * @param String $group Group Id, $uid (Unique) user id, $inviter original inviter
    *
    * @return True on success, False on failure, -1 on error.
    */
  public function addMember($group, $uid, $inviter) {
    // Add member
    $args = array(
      'clientVersion' => self::clientVersion,
      'groupName' => $group,
      'groupUuid' => '',
      'subjectId' => $uid,
      'subjectSourceId' => '',
      'subjectIdentifier' => '',
      'actAsSubjectId' => $inviter
    );

	try {
		$result = $this->_client->addMemberLite($args)->return;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('addMemberLite', $result);

    if ($result->resultMetadata->success == 'T') {
      return TRUE;
    } else {
      return FALSE;
    }
  }

  /**
    * Delete Member
    *
    * @param String $uid (Unique) uid, $group Group id
    *
    * @return True on success, False on failure or -1 on error.
    */
  public function deleteMember($uid, $group) {
    // Delete member
    $args = array(
      'clientVersion' => self::clientVersion,
      'groupName' => $group,
      'groupUuid' => '',
      'subjectId' => $uid,
      'subjectSourceId' => '',
      'subjectIdentifier' => '',
      'actAsSubjectId' => $this->_uuid
    );

	try {
		$result = $this->_client->deleteMemberLite($args)->return;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('deleteMemberLite', $result);

    if ($result->resultMetadata->success == 'T') {
      return TRUE;
    } else {
      return FALSE;
    }
  }

  /**
    * Set Privilege for a user for a group
    *
    * @param String $group Group id, $uid (Unique) user id, $privilege, $value
    $ $privilege is one of admin, update, read, view, optin, optout
    *
    * @return True on success, False on failure or -1 on error.
    */
  public function setPrivilege($group, $uid, $privilege, $value) {
    if ($value) $allowed = "T";
    else $allowed = "F";

    // Apply admin rights for member
    $args = array(
      'clientVersion' => self::clientVersion,
      'subjectId' => $uid,
      'subjectSourceId' => '',
      'subjectIdentifier' => '',
      'groupName' => $group,
      'groupUuid' => '',
      'stemName' => '',
      'stemUuid' => '',
      'privilegeType' => 'access',
      'privilegeName' => $privilege,
      'allowed' => $allowed,
      'actAsSubjectId' => 'GrouperSystem'
//      'actAsSubjectId' => $this->_uuid
    );

	try {
		$result = $this->_client->assignGrouperPrivilegesLite($args)->return;
	} catch (Exception $e) {
		error_log($e->getMessage());
		return -1;
	}

	$this->debugCall('assignGrouperPrivilegesLite', $result);

    if ($result->resultMetadata->success == 'T') {
      return TRUE;
    } else {
      return FALSE;
    }

  }

}


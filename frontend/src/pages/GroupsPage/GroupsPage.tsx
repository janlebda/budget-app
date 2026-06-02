import React, { useEffect, useState } from "react";
import { groupsApi } from "../../api/groupsApi";
import { useAuth } from "../../context/AuthContext";
import styles from "./Group.module.scss";
import GroupMembersPage from "./GroupMembersPage";
import { toast } from "react-toastify";
import ConfirmModal from "../../components/ConfirmModal/ConfirmModal";

interface Group {
  id: number | string;
  name: string;
  ownerId: number | string;
}

const GroupsPage: React.FC = () => {
  const { user } = useAuth();
  const [groups, setGroups] = useState<Group[]>([]);
  const [newGroupName, setNewGroupName] = useState("");
  const [selectedGroup, setSelectedGroup] = useState<Group | null>(null);
  const [groupToDelete, setGroupToDelete] = useState<Group | null>(null);

  const fetchGroupsData = async () => {
    return groupsApi.getGroups();
  };

  const refreshGroups = async () => {
    const data = await groupsApi.getGroups();
    setGroups(data);
  };

  useEffect(() => {
    let ignore = false;

    fetchGroupsData().then((data) => {
      if (ignore) return;
      setGroups(data);
    });

    return () => {
      ignore = true;
    };
  }, []);

  const handleCreateGroup = async () => {
    if (!user || !newGroupName.trim()) return;
    const createdGroup = await groupsApi.createGroup(newGroupName);
    setNewGroupName("");
    setSelectedGroup(createdGroup);
    await refreshGroups();
  };

  const handleDeleteGroup = async () => {
    if (!groupToDelete) return;

    try {
      await groupsApi.deleteGroup(groupToDelete.id);
      toast.success("Grupa usunięta.");
      setGroupToDelete(null);
      refreshGroups();
      setSelectedGroup(null);
    } catch (error) {
      console.error("Błąd usuwania grupy:", error);
      toast.error("Nie udało się usunąć grupy.");
    }
  };

  return (
    <div className={styles.container}>
      <h2>Twoje Grupy</h2>

      <div className={styles.form}>
        <input
          type="text"
          placeholder="Nazwa grupy"
          value={newGroupName}
          onChange={(e) => setNewGroupName(e.target.value)}
        />
        <button onClick={handleCreateGroup}>Utwórz Grupę</button>
      </div>

      <ul className={styles.list}>
        {groups.map((group) => (
          <li
            key={group.id}
            onClick={() => setSelectedGroup(group)}
            className={styles.groupItem}
          >
            {group.name}
            {String(user?.id) === String(group.ownerId) && (
              <button
              onClick={(e) => {
                e.stopPropagation();
                setGroupToDelete(group);
              }}
              className={styles.deleteButton}
            >
              Usuń
              </button>
            )}
          </li>
        ))}
      </ul>

      {selectedGroup && (
        <GroupMembersPage
          key={String(selectedGroup.id)}
          group={selectedGroup}
          onBack={() => setSelectedGroup(null)}
        />
      )}

      <ConfirmModal
        visible={Boolean(groupToDelete)}
        title="Usuń grupę"
        message="Czy na pewno chcesz usunąć tę grupę wraz z powiązanymi danymi?"
        confirmLabel="Usuń"
        onConfirm={handleDeleteGroup}
        onCancel={() => setGroupToDelete(null)}
      />
    </div>
  );
};

export default GroupsPage;

import graphqlClient from "./graphClient";

export type Id = number | string;

interface Group {
  id: Id;
  name: string;
  ownerId: Id;
}

interface GroupMember {
  id: Id;
  userId: Id;
  groupId: Id;
  userEmail: string;
}

export interface GroupDebt {
  id: Id;
  title: string;
  amount: number;
  paidByDebtor: boolean;
  confirmedByCreditor: boolean;
  debtor: { id: Id; email: string };
  creditor: { id: Id; email: string };
}

const isGroupDebtsNullError = (error: unknown) =>
  error instanceof Error &&
  error.message.includes("field at path '/groupDebts'") &&
  error.message.includes("non null type");

export const groupsApi = {
  getGroups: async () => {
    const query = `
      query {
        myGroups {
          id
          name
          ownerId
        }
      }
    `;
    const response = await graphqlClient<{ myGroups: Group[] }>(query);
    return response.data.myGroups;
  },

  createGroup: async (name: string) => {
    const mutation = `
      mutation CreateGroup($groupDTO: GroupInput!) {
        createGroup(groupDTO: $groupDTO) {
          id
          name
          ownerId
        }
      }
    `;
    const response = await graphqlClient<{ createGroup: Group }>(mutation, {
      groupDTO: { name },
    });
    return response.data.createGroup;
  },

  getGroupMembers: async (groupId: Id) => {
    const query = `
      query($groupId: ID!) {
        groupMembers(groupId: $groupId) {
          id
          userId
          groupId
          userEmail
        }
      }
    `;
    const response = await graphqlClient<{ groupMembers: GroupMember[] }>(
      query,
      { groupId }
    );
    return response.data.groupMembers;
  },

  addMember: async (groupId: Id, userEmail: string) => {
    const mutation = `
      mutation($membershipDTO: MembershipInput!) {
        addMember(membershipDTO: $membershipDTO) {
          id
          groupId
          userId
        }
      }
    `;
    return graphqlClient(mutation, { membershipDTO: { groupId, userEmail } });
  },

  deleteGroup: async (id: Id) => {
    const mutation = `
      mutation($id: ID!) {
        deleteGroup(id: $id)
      }
    `;
    return graphqlClient(mutation, { id });
  },

  removeMember: async (membershipId: Id) => {
    const mutation = `
      mutation($membershipId: ID!) {
        removeMember(membershipId: $membershipId)
      }
    `;
    return graphqlClient(mutation, { membershipId });
  },

  addGroupTransaction: async (
    groupId: Id,
    amount: number,
    type: string,
    title: string,
    selectedUserIds?: Id[]
  ) => {
    const mutation = `
      mutation($groupTransactionDTO: GroupTransactionInput!) {
        addGroupTransaction(groupTransactionDTO: $groupTransactionDTO)
      }
    `;
    return graphqlClient(mutation, {
      groupTransactionDTO: { groupId, amount, type, title, selectedUserIds },
    });
  },

  createDebt: async (
    groupId: Id,
    debtorId: Id,
    creditorId: Id,
    amount: number,
    title: string
  ) => {
    const mutation = `
      mutation($debtDTO: DebtInput!) {
        createDebt(debtDTO: $debtDTO) {
          id
          title
          amount
          paidByDebtor
          confirmedByCreditor
          debtor {
            id
            email
          }
          creditor {
            id
            email
          }
        }
      }
    `;
    const response = await graphqlClient<{ createDebt: GroupDebt }>(mutation, {
      debtDTO: { groupId, debtorId, creditorId, amount, title },
    });
    return response.data.createDebt;
  },

  deleteDebt: async (debtId: Id) => {
    const mutation = `
      mutation($debtId: ID!) {
        deleteDebt(debtId: $debtId)
      }
    `;
    return graphqlClient(mutation, { debtId });
  },

  getDebts: async (groupId: Id): Promise<GroupDebt[]> => {
    const query = `
      query($groupId: ID!) {
        groupDebts(groupId: $groupId) {
          id
          title
          amount
          paidByDebtor
          confirmedByCreditor
          debtor {
            id
            email
          }
          creditor {
            id
            email
          }
        }
      }
    `;
    try {
      const response = await graphqlClient<{ groupDebts: GroupDebt[] | null }>(
        query,
        { groupId }
      );
      return response.data.groupDebts ?? [];
    } catch (error) {
      if (isGroupDebtsNullError(error)) {
        console.warn("Backend returned null for groupDebts. Showing an empty debts list.");
        return [];
      }

      throw error;
    }
  },

  markDebtAsPaid: async (debtId: Id) => {
    const mutation = `
      mutation($debtId: ID!) {
        markDebtAsPaid(debtId: $debtId) {
          id
          title
          amount
          paidByDebtor
          confirmedByCreditor
          debtor {
            id
            email
          }
          creditor {
            id
            email
          }
        }
      }
    `;
    const response = await graphqlClient<{ markDebtAsPaid: GroupDebt }>(
      mutation,
      { debtId }
    );
    return response.data.markDebtAsPaid;
  },

  confirmDebtPayment: async (debtId: Id) => {
    const mutation = `
      mutation($debtId: ID!) {
        confirmDebtPayment(debtId: $debtId) {
          id
          title
          amount
          paidByDebtor
          confirmedByCreditor
          debtor {
            id
            email
          }
          creditor {
            id
            email
          }
        }
      }
    `;
    const response = await graphqlClient<{ confirmDebtPayment: GroupDebt }>(
      mutation,
      { debtId }
    );
    return response.data.confirmDebtPayment;
  },
};

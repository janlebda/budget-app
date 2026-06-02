// src/components/Layout/Layout.tsx
import { type ReactNode } from 'react';
import Navbar from '../Navbar/Navbar';
import styles from './Layout.module.scss';

interface LayoutProps {
  children: ReactNode;
}

const Layout = ({ children }: LayoutProps) => {
  return (
    <div className={styles.layout}>
      <Navbar />
      <main className={styles.main}>
        {children}
      </main>
    </div>
  );
};

export default Layout;

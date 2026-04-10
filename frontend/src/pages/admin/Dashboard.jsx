import { Link } from 'react-router-dom'
import Navbar from '../../components/Navbar'

const AdminDashboard = () => {
  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Admin Dashboard</h1>
        
        <div className="card">
          <h2>Admin Tools</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px', marginTop: '20px' }}>
            <Link to="/admin/users" className="btn btn-primary">Manage Users</Link>
            <Link to="/admin/tickets" className="btn btn-primary">Manage Tickets</Link>
            <Link to="/admin/disputes" className="btn btn-primary">Manage Disputes</Link>
          </div>
        </div>
      </div>
    </>
  )
}

export default AdminDashboard

import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { searchService } from '../../services/searchService'
import Navbar from '../../components/Navbar'

const SearchHelpers = () => {
  const [helpers, setHelpers] = useState([])
  const [filters, setFilters] = useState({
    categoryId: '',
    minRating: '',
    maxPrice: '',
    language: '',
    availableNow: false,
    cityId: ''
  })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    searchHelpers()
  }, [])

  const searchHelpers = async () => {
    setLoading(true)
    try {
      const data = await searchService.searchHelpers(filters)
      setHelpers(data)
    } catch (err) {
      console.error('Failed to search helpers', err)
    } finally {
      setLoading(false)
    }
  }

  const handleFilterChange = (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setFilters({ ...filters, [e.target.name]: value })
  }

  const handleSearch = (e) => {
    e.preventDefault()
    searchHelpers()
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Find a Helper</h1>
        
        <div className="card">
          <h3>Search Filters</h3>
          <form onSubmit={handleSearch}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
              <div className="form-group">
                <label>Min Rating</label>
                <input
                  type="number"
                  name="minRating"
                  min="1"
                  max="5"
                  step="0.1"
                  value={filters.minRating}
                  onChange={handleFilterChange}
                  placeholder="e.g., 4.0"
                />
              </div>
              <div className="form-group">
                <label>Max Price</label>
                <input
                  type="number"
                  name="maxPrice"
                  min="0"
                  step="0.01"
                  value={filters.maxPrice}
                  onChange={handleFilterChange}
                  placeholder="e.g., 50.00"
                />
              </div>
              <div className="form-group">
                <label>Language</label>
                <input
                  type="text"
                  name="language"
                  value={filters.language}
                  onChange={handleFilterChange}
                  placeholder="e.g., English"
                />
              </div>
              <div className="form-group">
                <label style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                  <input
                    type="checkbox"
                    name="availableNow"
                    checked={filters.availableNow}
                    onChange={handleFilterChange}
                    style={{ width: 'auto' }}
                  />
                  Available Now
                </label>
              </div>
            </div>
            <button type="submit" className="btn btn-primary" style={{ marginTop: '10px' }}>
              Search
            </button>
          </form>
        </div>

        <div className="card">
          <h3>Available Helpers</h3>
          {loading ? (
            <div className="loading">Searching...</div>
          ) : helpers.length === 0 ? (
            <p>No helpers found matching your criteria.</p>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
              {helpers.map(helper => (
                <div key={helper.id} className="card" style={{ margin: 0, display: 'flex', flexDirection: 'column' }}>
                  <h4>{helper.firstName} {helper.lastName}</h4>
                  <p><strong>Headline:</strong> {helper.professionalHeadline || 'No headline'}</p>
                  <p><strong>Languages:</strong> {helper.languagesSpoken || 'Not specified'}</p>
                  <p><strong>Location:</strong> {helper.cityName}, {helper.countryName}</p>
                  <p><strong>Available:</strong> {helper.isAvailable ? 'Yes' : 'No'}</p>
                  {helper.specializations && helper.specializations.length > 0 && (
                    <div style={{ marginBottom: '15px' }}>
                      <strong>Services:</strong>
                      <ul style={{ marginTop: '5px', paddingLeft: '20px', marginBottom: '0' }}>
                        {helper.specializations.map((spec, idx) => (
                          <li key={idx}>{spec.categoryName} - ${spec.hourlyRate}/hr</li>
                        ))}
                      </ul>
                    </div>
                  )}
                  <Link to={`/user/helper/${helper.id}`} className="btn btn-primary" style={{ marginTop: 'auto' }}>
                    View Profile
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default SearchHelpers

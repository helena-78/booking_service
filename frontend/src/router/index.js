import { createRouter, createWebHistory } from 'vue-router'

import AllActivities from '../views/activities/AllActivities.vue'
import CreateActivity from '../views/activities/CreateActivity.vue'
import AvailableSlots from '../views/scheduling/AvailableSlots.vue'
import ReserveSlot from '../views/scheduling/ReserveSlot.vue'
import AccountLogin from '../views/account/Login.vue'
import AccountRegister from '../views/account/Register.vue'
import UserProfile from '../views/account/UserProfile.vue'
import UpdateUserStatus from '../views/account/UpdateUserStatus.vue'
import SportCenter from '../views/account/SportCenter.vue'
import ModerationCases from '../views/moderation/Cases.vue'
import CreateCase from '../views/moderation/CreateCase.vue'
import CaseDetail from '../views/moderation/CaseDetail.vue'
import ApplySanction from '../views/moderation/ApplySanction.vue'
import SanctionHistory from '../views/moderation/SanctionHistory.vue'
import UserRatings from '../views/ratings/UserRatings.vue'
import FacilitySearch from '../views/search/FacilitySearch.vue'
import UserSuggestion from '../views/matching/UserSuggestion.vue'
import CrossEntitySearch from '../views/search/CrossEntitySearch.vue'
import UserSearch from '../views/search/UserSearch.vue'
import ActivitySearch from '../views/search/ActivitySearch.vue'
import IndexEntry from '../views/search/IndexEntry.vue'
import AllBookings from '../views/bookings/AllBookings.vue'
import CreateBooking from '../views/bookings/CreateBooking.vue'
import BookingFacilitySearch from '../views/bookings/BookingFacilitySearch.vue'
import AdminRatings from '../views/ratings/AdminRatings.vue'
import ActivityDetail from '../views/activities/ActivityDetail.vue'
import UserAvailability from '../views/scheduling/UserAvailability.vue'
import ResolveSlots from '../views/scheduling/ResolveSlots.vue'
import ActivitySuggestion from '../views/matching/ActivitySuggestion.vue'
import CompatibilityCheck from '../views/matching/CompatibilityCheck.vue'
import SuggestionManagement from '@/views/matching/SuggestionManagement.vue'


const routes = [
  {
    path: '/',
    redirect: '/activities'
  },
  {
    path: '/activities',
    name: 'AllActivities',
    component: AllActivities
  },
  {
    path: '/activities/new',
    name: 'CreateActivity',
    component: CreateActivity
  },
  {
    path: '/activities/:id',
    name: 'ActivityDetail',
    component: ActivityDetail
  },
  {
    path: '/slots',
    name: 'AvailableSlots',
    component: AvailableSlots
  },
  {
    path: '/slots/reserve',
    name: 'ReserveSlot',
    component: ReserveSlot
  },
  {
    path: '/slots/availability',
    name: 'UserAvailability',
    component: UserAvailability
  },
  {
    path: '/slots/resolve',
    name: 'ResolveSlots',
    component: ResolveSlots
  },
  {
    path: '/account/login',
    name: 'AccountLogin',
    component: AccountLogin
  },
  {
    path: '/account/register',
    name: 'AccountRegister',
    component: AccountRegister
  },
  {
    path: '/account/profile',
    name: 'UserProfile',
    component: UserProfile
  },
  {
    path: '/account/status',
    name: 'UpdateUserStatus',
    component: UpdateUserStatus
  },
  {
    path: '/account/sport-center',
    name: 'SportCenter',
    component: SportCenter
  },
  {
    path: '/moderation/cases',
    name: 'ModerationCases',
    component: ModerationCases
  },
  {
    path: '/moderation/cases/new',
    name: 'CreateCase',
    component: CreateCase
  },
  {
    path: '/moderation/cases/:id',
    name: 'CaseDetail',
    component: CaseDetail
  },
  {
    path: '/moderation/sanctions/new',
    name: 'ApplySanction',
    component: ApplySanction
  },
  {
    path: '/moderation/sanctions/history',
    name: 'SanctionHistory',
    component: SanctionHistory
  },
  {
    path: '/ratings',
    name: 'UserRatings',
    component: UserRatings
  },
  
  {
    path: '/matching/users',
    name: 'UserSuggestion',
    component: UserSuggestion
  },
  {
    path: '/matching/activities',
    name: 'ActivitySuggestion',
    component: ActivitySuggestion
  },
  {
    path: '/admin/ratings',
    name: 'AdminRatings',
    component: AdminRatings
  },
  {
    path: '/matching/management',
    name: 'SuggestionManagement',
    component: SuggestionManagement
  },
  {
    path: '/matching/compatibility',
    name: 'CompatibilityCheck',
    component: CompatibilityCheck
  },

  // -----------------------------------------------------------------
  // Search Service - one route per REST endpoint in the controller.
  // -----------------------------------------------------------------
  {
    path: '/search',
    name: 'CrossEntitySearch',
    component: CrossEntitySearch
  },
  {
    path: '/search/users',
    name: 'UserSearch',
    component: UserSearch
  },
  {
    path: '/search/activities',
    name: 'ActivitySearch',
    component: ActivitySearch
  },
  {
    path: '/search/facilities',
    name: 'FacilitySearch',
    component: FacilitySearch
  },
  {
    path: '/search/index',
    name: 'IndexEntry',
    component: IndexEntry
  },

  {
    path: '/bookings',
    name: 'AllBookings',
    component: AllBookings
  },
  {
    path: '/bookings/new',
    name: 'CreateBooking',
    component: CreateBooking
  },
  {
    path: '/bookings/facilities',
    name: 'BookingFacilitySearch',
    component: BookingFacilitySearch
  },
  {
    // fallback for unknown routes — redirect to activities
    path: '/:catchAll(.*)',
    redirect: '/activities'
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
